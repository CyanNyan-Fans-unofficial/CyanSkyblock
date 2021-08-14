package com.cyannyan.cyansky.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.SeedCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SeedCommand.class)
public class SeedCommandMixin {
  /**
   * @author
   * @reason 
   */
  @Overwrite
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
    dispatcher.register(CommandManager.literal("seed").executes(context -> {
      long seed = context.getSource().getWorld().getSeed();
      Text text = Texts.bracketed((new LiteralText(String.valueOf(seed)))
          .styled(style -> style.withColor(Formatting.GREEN)
              .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(seed)))
              .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.copy.click")))
              .withInsertion(String.valueOf(seed))));
      context.getSource().sendFeedback(new TranslatableText("commands.seed.success", text), false);
      return (int)seed;
    }));
  }
}
