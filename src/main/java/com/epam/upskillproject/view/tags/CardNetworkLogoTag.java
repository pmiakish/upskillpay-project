package com.epam.upskillproject.view.tags;

import com.epam.upskillproject.init.PropertiesKeeper;
import com.epam.upskillproject.model.dto.CardNetworkType;
import jakarta.inject.Inject;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

public class CardNetworkLogoTag extends SimpleTagSupport {

    private static final String VISA_CLASSIC_PROP = "cardlogo.link.visa.classic";
    private static final String VISA_GOLD_PROP = "cardlogo.link.visa.gold";
    private static final String VISA_PLATINUM_PROP = "cardlogo.link.visa.platinum";
    private static final String MC_STANDARD_PROP = "cardlogo.link.mc.standard";
    private static final String MC_GOLD_PROP = "cardlogo.link.mc.gold";
    private static final String MC_WORLD_PROP = "cardlogo.link.mc.world";
    private static final String IMG_TAG_PATTERN_PROP = "cardlogo.tag.img";

    @Inject
    private PropertiesKeeper propertiesKeeper;

    private CardNetworkType network;

    @Override
    public void doTag() throws JspException, IOException {
        String logoLink;
        switch (network) {
            case VISA_CLASSIC:
                logoLink = propertiesKeeper.getStringOrDefault(VISA_CLASSIC_PROP, "");
                break;
            case VISA_GOLD:
                logoLink = propertiesKeeper.getStringOrDefault(VISA_GOLD_PROP, "");
                break;
            case VISA_PLATINUM:
                logoLink = propertiesKeeper.getStringOrDefault(VISA_PLATINUM_PROP, "");
                break;
            case MC_STANDARD:
                logoLink = propertiesKeeper.getStringOrDefault(MC_STANDARD_PROP, "");
                break;
            case MC_GOLD:
                logoLink = propertiesKeeper.getStringOrDefault(MC_GOLD_PROP, "");
                break;
            case MC_WORLD:
                logoLink = propertiesKeeper.getStringOrDefault(MC_WORLD_PROP, "");
                break;
            default:
                logoLink = "";
                break;
        }
        JspWriter writer = getJspContext().getOut();
        writer.println(
                String.format(propertiesKeeper.getString(IMG_TAG_PATTERN_PROP), logoLink, network)
        );
    }

    public void setNetwork(CardNetworkType network) {
        this.network = network;
    }
}
