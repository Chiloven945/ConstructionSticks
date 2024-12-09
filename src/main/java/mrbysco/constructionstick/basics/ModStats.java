package mrbysco.constructionstick.basics;

import mrbysco.constructionstick.ConstructionStick;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModStats {
	public static final DeferredRegister<ResourceLocation> CUSTOM_STATS = DeferredRegister.create(BuiltInRegistries.CUSTOM_STAT, ConstructionStick.MOD_ID);

	public static final ResourceLocation USE_STICK = ConstructionStick.modLoc("use_stick");
	public static final Supplier<ResourceLocation> USE_STICK_STAT = CUSTOM_STATS.register("use_stick", () -> USE_STICK);
}
