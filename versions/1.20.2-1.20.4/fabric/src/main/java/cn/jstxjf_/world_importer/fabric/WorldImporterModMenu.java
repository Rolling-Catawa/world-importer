package cn.jstxjf_.world_importer.fabric;

import cn.jstxjf_.world_importer.client.WIConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class WorldImporterModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return WIConfigScreen::new;
    }
}

