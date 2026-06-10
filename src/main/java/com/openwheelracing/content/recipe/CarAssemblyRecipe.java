package com.openwheelracing.content.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.openwheelracing.registry.OWRRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record CarAssemblyRecipe(
    Ingredient chassis,
    Ingredient engine,
    Ingredient tires,
    Ingredient aeroKit,
    Ingredient gearbox,
    Ingredient steeringControls,
    ItemStack result,
    int assemblyTime
) implements Recipe<CarAssemblyRecipe.Input> {
    @Override
    public boolean matches(Input input, Level level) {
        return chassis.test(input.getItem(0))
            && engine.test(input.getItem(1))
            && tires.test(input.getItem(2))
            && aeroKit.test(input.getItem(3))
            && gearbox.test(input.getItem(4))
            && steeringControls.test(input.getItem(5));
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<Input>> getSerializer() {
        return OWRRecipes.CAR_ASSEMBLY_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<Input>> getType() {
        return OWRRecipes.CAR_ASSEMBLY_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public record Input(ItemStack chassis, ItemStack engine, ItemStack tires, ItemStack aeroKit, ItemStack gearbox, ItemStack steeringControls) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return switch (index) {
                case 0 -> chassis;
                case 1 -> engine;
                case 2 -> tires;
                case 3 -> aeroKit;
                case 4 -> gearbox;
                case 5 -> steeringControls;
                default -> ItemStack.EMPTY;
            };
        }

        @Override
        public int size() {
            return 6;
        }
    }

    public static class Serializer implements RecipeSerializer<CarAssemblyRecipe> {
        private static final MapCodec<CarAssemblyRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("chassis").forGetter(CarAssemblyRecipe::chassis),
            Ingredient.CODEC.fieldOf("engine").forGetter(CarAssemblyRecipe::engine),
            Ingredient.CODEC.fieldOf("tires").forGetter(CarAssemblyRecipe::tires),
            Ingredient.CODEC.fieldOf("aero_kit").forGetter(CarAssemblyRecipe::aeroKit),
            Ingredient.CODEC.fieldOf("gearbox").forGetter(CarAssemblyRecipe::gearbox),
            Ingredient.CODEC.fieldOf("steering_controls").forGetter(CarAssemblyRecipe::steeringControls),
            ItemStack.CODEC.fieldOf("result").forGetter(CarAssemblyRecipe::result),
            com.mojang.serialization.Codec.INT.optionalFieldOf("assembly_time", 100).forGetter(CarAssemblyRecipe::assemblyTime)
        ).apply(instance, CarAssemblyRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CarAssemblyRecipe> STREAM_CODEC = StreamCodec.of(
            (buffer, recipe) -> {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.chassis);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.engine);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.tires);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.aeroKit);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.gearbox);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.steeringControls);
                ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
                buffer.writeInt(recipe.assemblyTime);
            },
            buffer -> new CarAssemblyRecipe(
                Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                ItemStack.STREAM_CODEC.decode(buffer),
                buffer.readInt()
            )
        );

        @Override
        public MapCodec<CarAssemblyRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CarAssemblyRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
