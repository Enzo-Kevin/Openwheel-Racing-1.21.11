package com.openwheelracing.client.input;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.glfw.GLFW;

public final class WheelInputManager {
    private static final Map<WheelInputSettings.ButtonRole, Boolean> PREVIOUS_BUTTONS = new EnumMap<>(WheelInputSettings.ButtonRole.class);

    private WheelInputManager() {
    }

    public static List<Device> devices() {
        List<Device> devices = new ArrayList<>();
        for (int id = GLFW.GLFW_JOYSTICK_1; id <= GLFW.GLFW_JOYSTICK_LAST; id++) {
            if (GLFW.glfwJoystickPresent(id)) {
                String name = GLFW.glfwGetJoystickName(id);
                devices.add(new Device(id, name == null ? "Joystick " + id : name));
            }
        }
        return devices;
    }

    public static Device selectedDevice(WheelInputSettings settings) {
        if (settings == null || !settings.enabled) {
            return null;
        }
        if (settings.selectedJoystickId >= GLFW.GLFW_JOYSTICK_1 && settings.selectedJoystickId <= GLFW.GLFW_JOYSTICK_LAST && GLFW.glfwJoystickPresent(settings.selectedJoystickId)) {
            String name = GLFW.glfwGetJoystickName(settings.selectedJoystickId);
            return new Device(settings.selectedJoystickId, name == null ? settings.selectedJoystickName : name);
        }
        if (!settings.selectedJoystickName.isBlank()) {
            for (Device device : devices()) {
                if (settings.selectedJoystickName.equals(device.name())) {
                    settings.selectedJoystickId = device.id();
                    return device;
                }
            }
        }
        return null;
    }

    public static Output poll(WheelInputSettings settings) {
        Device device = selectedDevice(settings);
        if (device == null) {
            PREVIOUS_BUTTONS.clear();
            return Output.NEUTRAL;
        }
        FloatBuffer axes = GLFW.glfwGetJoystickAxes(device.id());
        ByteBuffer buttons = GLFW.glfwGetJoystickButtons(device.id());
        if (axes == null) {
            return Output.NEUTRAL;
        }

        float steering = settings.steering.axis >= 0 ? transformSteering(axis(axes, settings.steering.axis), settings.steering) : 0.0f;
        float throttle;
        float brake;
        if (settings.combinedPedals && settings.combinedPedal.axis >= 0) {
            float combined = transformSteering(axis(axes, settings.combinedPedal.axis), settings.combinedPedal);
            throttle = Math.max(0.0f, combined);
            brake = Math.max(0.0f, -combined);
        } else {
            throttle = settings.throttle.axis >= 0 ? transformPedal(axis(axes, settings.throttle.axis), settings.throttle) : 0.0f;
            brake = settings.brake.axis >= 0 ? transformPedal(axis(axes, settings.brake.axis), settings.brake) : 0.0f;
        }

        EnumMap<WheelInputSettings.ButtonRole, Boolean> pressed = new EnumMap<>(WheelInputSettings.ButtonRole.class);
        if (buttons != null) {
            for (WheelInputSettings.ButtonRole role : WheelInputSettings.ButtonRole.values()) {
                int index = settings.button(role);
                boolean down = index >= 0 && index < buttons.limit() && buttons.get(index) == GLFW.GLFW_PRESS;
                boolean wasDown = PREVIOUS_BUTTONS.getOrDefault(role, false);
                pressed.put(role, down && !wasDown);
                PREVIOUS_BUTTONS.put(role, down);
            }
        }
        return new Output(throttle, brake, steering, pressed);
    }

    public static int detectMovedAxis(int joystickId, float[] baseline, float threshold) {
        FloatBuffer axes = GLFW.glfwGetJoystickAxes(joystickId);
        if (axes == null || baseline == null) {
            return -1;
        }
        int count = Math.min(axes.limit(), baseline.length);
        int bestAxis = -1;
        float bestDelta = threshold;
        for (int i = 0; i < count; i++) {
            float delta = Math.abs(axes.get(i) - baseline[i]);
            if (delta > bestDelta) {
                bestDelta = delta;
                bestAxis = i;
            }
        }
        return bestAxis;
    }

    public static int detectPressedButton(int joystickId) {
        ByteBuffer buttons = GLFW.glfwGetJoystickButtons(joystickId);
        if (buttons == null) {
            return -1;
        }
        for (int i = 0; i < buttons.limit(); i++) {
            if (buttons.get(i) == GLFW.GLFW_PRESS) {
                return i;
            }
        }
        return -1;
    }

    public static float[] axisSnapshot(int joystickId) {
        FloatBuffer axes = GLFW.glfwGetJoystickAxes(joystickId);
        if (axes == null) {
            return new float[0];
        }
        float[] snapshot = new float[axes.limit()];
        for (int i = 0; i < axes.limit(); i++) {
            snapshot[i] = axes.get(i);
        }
        return snapshot;
    }

    public static float rawAxis(int joystickId, int axis) {
        FloatBuffer axes = GLFW.glfwGetJoystickAxes(joystickId);
        return axes == null ? 0.0f : axis(axes, axis);
    }

    public static float previewAxis(float raw, WheelInputSettings.AxisBinding binding, boolean pedal) {
        return pedal ? transformPedal(raw, binding) : transformSteering(raw, binding);
    }

    private static float axis(FloatBuffer axes, int index) {
        if (index < 0 || index >= axes.limit()) {
            return 0.0f;
        }
        return axes.get(index);
    }

    private static float transformPedal(float raw, WheelInputSettings.AxisBinding binding) {
        float range = binding.max - binding.min;
        if (Math.abs(range) < 0.001f) {
            return 0.0f;
        }
        float value = (raw - binding.rest) / (binding.max - binding.rest);
        if (Math.abs(binding.max - binding.rest) < 0.001f) {
            value = (raw - binding.rest) / range;
        }
        if (binding.invert) {
            value = -value;
        }
        value = Math.max(0.0f, value);
        value = applyDeadzone(value, binding.deadzone);
        value = applyCurve(value, binding.linearity) * binding.sensitivity;
        return WheelInputSettings.clamp(value, 0.0f, 1.0f);
    }

    private static float transformSteering(float raw, WheelInputSettings.AxisBinding binding) {
        float leftRange = binding.rest - binding.min;
        float rightRange = binding.max - binding.rest;
        float value;
        if (raw < binding.rest) {
            value = leftRange < 0.001f ? 0.0f : -(binding.rest - raw) / leftRange;
        } else {
            value = rightRange < 0.001f ? 0.0f : (raw - binding.rest) / rightRange;
        }
        if (binding.invert) {
            value = -value;
        }
        float sign = Math.signum(value);
        value = applyDeadzone(Math.abs(value), binding.deadzone);
        value = applyCurve(value, binding.linearity) * binding.sensitivity;
        return WheelInputSettings.clamp(sign * value, -1.0f, 1.0f);
    }

    private static float applyDeadzone(float value, float deadzone) {
        if (value <= deadzone) {
            return 0.0f;
        }
        return (value - deadzone) / (1.0f - deadzone);
    }

    private static float applyCurve(float value, float linearity) {
        return (float) Math.pow(WheelInputSettings.clamp(value, 0.0f, 1.0f), linearity);
    }

    public record Device(int id, String name) {
    }

    public record Output(float throttle, float brake, float steering, Map<WheelInputSettings.ButtonRole, Boolean> pressedButtons) {
        public static final Output NEUTRAL = new Output(0.0f, 0.0f, 0.0f, Map.of());

        public boolean pressed(WheelInputSettings.ButtonRole role) {
            return pressedButtons.getOrDefault(role, false);
        }
    }
}
