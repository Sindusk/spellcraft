package mod.sin.spellcraft;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpellcraftStatuetteTweaks
{
    private static final Logger logger;

    static
    {
        logger = Logger.getLogger(SpellcraftStatuetteTweaks.class.getName());
    }

    public static void patchCastingCalls()
    {
        try
        {
        	ClassPool classPool = HookManager.getInstance().getClassPool();
        	//Class<SpellcraftStatuetteTweaks> thisClass = SpellcraftStatuetteTweaks.class;
        	
	    	List<String> classes = new ArrayList<>();
	        classes.add("com.wurmonline.server.behaviours.BodyPartBehaviour");
	        classes.add("com.wurmonline.server.behaviours.CreatureBehaviour");
	        classes.add("com.wurmonline.server.behaviours.ItemBehaviour");
	        classes.add("com.wurmonline.server.behaviours.TileBehaviour");
	        classes.add("com.wurmonline.server.behaviours.TileBorderBehaviour");
	        classes.add("com.wurmonline.server.behaviours.WoundBehaviour");
	
	        ExprEditor e = new ExprEditor(){
	            @Override
	            public void edit(MethodCall m) throws CannotCompileException
	            {
	                if (m.getClassName().equals("com.wurmonline.server.behaviours.Methods") && m.getMethodName().equals("castSpell"))
	                {
	                    m.replace("if($2.religious){"
	                    		+ "  $_ = com.wurmonline.server.spells.SpellHelper.castSpell($$, source);"
	                    		+ "}else{"
	                    		+ "  $_ = $proceed($$);"
	                    		+ "}");
	                }
	            }
	        };
	
	        for (String classDescription : classes) {
	            CtClass ctClass = classPool.get(classDescription);
	            CtMethod[] methods = ctClass.getDeclaredMethods("action");
	            for (CtMethod method : methods) {
	                try
	                {
	                    method.instrument(e);
	                } catch (Exception ex) {
	                    logger.warning(ex.toString());
	                }
	            }
	        }
        } catch (NotFoundException ex)
        {
            logger.log(Level.SEVERE, null, (Throwable) ex);
            throw new HookException(ex);
        }
    }

    public static void patchSpellClass()
    {
    	try{
	        CtClass cSpell = HookManager.getInstance().getClassPool().get("com.wurmonline.server.spells.Spell");
	
	        for (CtMethod method : cSpell.getDeclaredMethods("run"))
	        {
	            patchSpellRunMethod(method);
	        }
	    } catch (CannotCompileException | NotFoundException ex)
	    {
	        logger.log(Level.SEVERE, null, (Throwable) ex);
	        throw new HookException(ex);
	    }
    }

    private static void patchSpellRunMethod(CtMethod method) throws CannotCompileException
    {
        MethodInfo methodInfo = method.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute table = (LocalVariableAttribute) codeAttribute.getAttribute(javassist.bytecode.LocalVariableAttribute.tag);
        int varN = table.tableLength();

        String bonusName = "bonus";

        for (int i = 0; i < varN; ++i)
        {
            if (table.variableName(i).equals("attbonus"))
            {
                bonusName = "attbonus";
                break;
            }
        }

        method.instrument(new SpellRunEditor(bonusName));
    }

    static class SpellRunEditor extends ExprEditor
    {
        private String bonusName;

        SpellRunEditor(String bonusName)
        {
            this.bonusName = bonusName;
        }

        @Override
        public void edit(MethodCall m) throws CannotCompileException
        {
            if (m.getClassName().equals("com.wurmonline.server.spells.Spell"))
            {
                if (m.getMethodName().equals("doEffect") || m.getMethodName().equals("doNegativeEffect"))
                {
                    m.replace("{\n"
                            + "   if(this.isReligious()){"
                            + "     $2 = $2 + com.wurmonline.server.spells.SpellHelper.getStatuettePowerIncrement(performer);\n"
                            + "   }"
                            + "   $_ = $proceed($$);\n" +
                            "}\n");
                } else if (m.getMethodName().equals("postcondition"))
                {
                    m.replace("if(this.isReligious()){"
                    		+ "  $_ = $proceed($1, $2, $3, $4 + com.wurmonline.server.spells.SpellHelper.getStatuettePowerIncrement(performer));"
                    		+ "}else{"
                    		+ "  $_ = $proceed($$);"
                    		+ "}");
                } else if (m.getMethodName().equals("trimPower"))
                {
                    m.replace("{\n"
                    		+ "  if(this.isReligious()){"
                    		+ "    " + bonusName + " += com.wurmonline.server.spells.SpellHelper.getStatuetteBonus(performer);\n"
            				+ "  }"
                            + "  $_ = $proceed($$);\n" +
                            "}\n");
                }
            }
        }
    }
}
