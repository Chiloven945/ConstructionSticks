package mrbysco.constructionstick.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mrbysco.constructionstick.api.IStickTemplate;
import mrbysco.constructionstick.basics.StickUtil;
import mrbysco.constructionstick.basics.option.StickOptions;
import mrbysco.constructionstick.config.ConstructionConfig;
import mrbysco.constructionstick.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

import java.util.stream.Stream;

/**
 * A copy of SmithingTransformRecipe specifically for applying upgrades to Construction Sticks
 */
public class SmithingApplyUpgradeRecipe implements SmithingRecipe {
	final Ingredient template;
	final Ingredient base;
	final Ingredient addition;
	final ItemStack result;
	final IStickTemplate upgrade;

	public SmithingApplyUpgradeRecipe(Ingredient template, Ingredient base, Ingredient addition, ItemStack result, IStickTemplate upgrade) {
		this.template = template;
		this.base = base;
		this.addition = addition;
		this.result = result;
		this.upgrade = upgrade;
	}

	public SmithingApplyUpgradeRecipe(Ingredient template, Ingredient base, Ingredient addition, ItemStack result, ResourceLocation upgrade) {
		this(template, base, addition, result, StickUtil.getUpgrade(upgrade).orElseThrow(() -> new IllegalArgumentException("Unknown upgrade: " + upgrade)));
	}

	public boolean matches(SmithingRecipeInput input, Level level) {
		ItemStack base = input.base();
		if (base.has(upgrade.getStickComponent()) || !new StickOptions(base).upgrades.isCompatible(upgrade) || !ConstructionConfig.getStickProperties(base.getItem()).isUpgradeable())
			return false;
		return this.template.test(input.template()) && this.base.test(base) && this.addition.test(input.addition());
	}

	public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
		ItemStack itemstack = input.base().transmuteCopy(this.result.getItem(), this.result.getCount());
		itemstack.applyComponents(this.result.getComponentsPatch());
		return itemstack;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return this.result;
	}

	@Override
	public boolean isTemplateIngredient(ItemStack stack) {
		return this.template.test(stack);
	}

	@Override
	public boolean isBaseIngredient(ItemStack stack) {
		return this.base.test(stack);
	}

	@Override
	public boolean isAdditionIngredient(ItemStack stack) {
		return this.addition.test(stack);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.SMITHING_UPGRADE.get();
	}

	@Override
	public boolean isIncomplete() {
		return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::hasNoItems);
	}

	public Ingredient getBase() {
		return base;
	}

	public Ingredient getAddition() {
		return addition;
	}

	public Ingredient getTemplate() {
		return template;
	}

	public static class Serializer implements RecipeSerializer<SmithingApplyUpgradeRecipe> {
		private static final MapCodec<SmithingApplyUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(
				p_340782_ -> p_340782_.group(
								Ingredient.CODEC.fieldOf("template").forGetter(p_301310_ -> p_301310_.template),
								Ingredient.CODEC.fieldOf("base").forGetter(p_300938_ -> p_300938_.base),
								Ingredient.CODEC.fieldOf("addition").forGetter(p_301153_ -> p_301153_.addition),
								ItemStack.STRICT_CODEC.fieldOf("result").forGetter(p_300935_ -> p_300935_.result),
								ResourceLocation.CODEC.fieldOf("upgrade").forGetter(p_300935_ -> p_300935_.upgrade.getRegistryName())
						)
						.apply(p_340782_, SmithingApplyUpgradeRecipe::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, SmithingApplyUpgradeRecipe> STREAM_CODEC = StreamCodec.of(
				SmithingApplyUpgradeRecipe.Serializer::toNetwork, SmithingApplyUpgradeRecipe.Serializer::fromNetwork
		);

		@Override
		public MapCodec<SmithingApplyUpgradeRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SmithingApplyUpgradeRecipe> streamCodec() {
			return STREAM_CODEC;
		}

		private static SmithingApplyUpgradeRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
			Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
			Ingredient ingredient1 = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
			Ingredient ingredient2 = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
			ItemStack itemstack = ItemStack.STREAM_CODEC.decode(buffer);
			ResourceLocation upgrade = ResourceLocation.STREAM_CODEC.decode(buffer);
			return new SmithingApplyUpgradeRecipe(ingredient, ingredient1, ingredient2, itemstack, upgrade);
		}

		private static void toNetwork(RegistryFriendlyByteBuf buffer, SmithingApplyUpgradeRecipe recipe) {
			Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.template);
			Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.base);
			Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.addition);
			ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
			ResourceLocation.STREAM_CODEC.encode(buffer, recipe.upgrade.getRegistryName());
		}
	}
}
