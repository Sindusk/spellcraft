package com.wurmonline.server.questions;

import java.util.Properties;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.Question;
import com.wurmonline.shared.util.StringUtilities;

public class SummonSoulQuestion extends Question {
	private boolean properlySent = false;
    private double power;

    public SummonSoulQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, double power) {
        super(aResponder, aTitle, aQuestion, 79, aTarget);
        this.power = power;
    }

    @Override
    public void answer(Properties aAnswers) {
        if (!this.properlySent) {
            return;
        }
        String name = aAnswers.getProperty("name");
        Creature soul = null;
        if (name != null && name.length() > 1) {
            soul = SummonSoulQuestion.acquireSoul(StringUtilities.raiseFirstLetter((String)name), this.getResponder(), this.power);
        }
        if (soul == null) {
            this.getResponder().getCommunicator().sendNormalServerMessage("No such soul found.");
        }else{
        	SummonSoulAcceptQuestion ssaq = new SummonSoulAcceptQuestion(soul, "Accept Summon?", "Would you like to accept a summon from "+this.getResponder().getName()+"?", this.getResponder().getWurmId(), this.getResponder());
        	ssaq.sendQuestion();
        }
    }

    public static final Creature acquireSoul(String name, Creature responder, double power) {
        PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
        if (pinf != null && pinf.loaded) {
        	try {
				Creature player = Server.getInstance().getCreature(pinf.wurmId);
				return player;
			} catch (NoSuchPlayerException | NoSuchCreatureException e) {
				e.printStackTrace();
			}
        }
        return null;
    }

    @Override
    public void sendQuestion() {
        this.properlySent = true;
        StringBuilder sb = new StringBuilder();
        sb.append(this.getBmlHeader());
        sb.append("text{text='Which soul do you wish to summon?'};");
        sb.append("label{text='Name:'};input{id='name';maxchars='40';text=\"\"};");
        sb.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(300, 300, true, true, sb.toString(), 200, 200, 200, this.title);
    }
}
