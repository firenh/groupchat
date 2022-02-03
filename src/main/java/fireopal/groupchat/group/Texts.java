// package fireopal.groupchat.group;

// import fireopal.groupchat.GroupChat;
// import net.minecraft.text.LiteralText;
// import net.minecraft.text.Text;
// import net.minecraft.text.TranslatableText;

// public class Texts {
//     private static boolean useTranslatableText() {
//         return GroupChat.getConfig().useTranslatableText;
//     }

//     private static Text switchText(TranslatableText translatableText, LiteralText literalText) {
//         return useTranslatableText() ? translatableText : literalText;
//     }

//     private static Text switchText(String translatableText, String literalText) {
//         return useTranslatableText() ? new TranslatableText(translatableText) : new LiteralText(literalText);
//     }

//     public static Text getText(String string) {
//         switch (string) {
//             case "groupchat.group": return switchText(string, "Group");
//             case "groupchat.does_not_exist": return switchText(string, " does not exist!");
//             case
//         }
//     }
// }
