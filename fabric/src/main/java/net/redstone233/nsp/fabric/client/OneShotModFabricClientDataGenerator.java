package net.redstone233.nsp.fabric.client;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.redstone233.nsp.fabric.data.ModChineseLanguageProvider;
import net.redstone233.nsp.fabric.data.ModEnglishLanguageProvider;

public class OneShotModFabricClientDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(ModChineseLanguageProvider::new);
        pack.addProvider(ModEnglishLanguageProvider::new);
    }
}
