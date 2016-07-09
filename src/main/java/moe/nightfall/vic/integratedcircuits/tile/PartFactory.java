package moe.nightfall.vic.integratedcircuits.tile;

import java.util.HashMap;

import com.google.common.collect.Maps;
import mcmultipart.multipart.IPartFactory;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.Multipart;
import mcmultipart.multipart.MultipartRegistry;
import net.minecraft.util.ResourceLocation;

public class PartFactory implements IPartFactory {
	private static HashMap<String, Class<? extends Multipart>> parts = Maps.newHashMap();
	public static PartFactory instance = new PartFactory();

	private PartFactory() {
	}

	public static void register(String type, Class<? extends Multipart> clazz) {
		parts.put(type, clazz);
	}

	@Override
	public Multipart createPart(ResourceLocation type, boolean client) {
		Class clazz = parts.get(client);
		if (clazz == null)
			return null;
		try {
			return (Multipart) clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void initialize() {
		String[] keys = parts.keySet().toArray(new String[parts.keySet().size()]);
		MultipartRegistry.registerPartFactory(instance, keys);
	}
}
