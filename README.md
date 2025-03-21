**Variant Swap** is a mod that lets you change the variant of the item you're holding by using a **customizable key** (Z by default) and **scrolling**. When you scroll, you'll also see at the top of the screen a quick preview of what's before and after the selected item (for a few seconds).

In **Creative** mode, you can switch through every available variant of the item, while in **Survival** mode it only shows items you have in your inventory. **It works with both vanilla and modded items**!

<img src="https://i.imgur.com/1L14Ev0.gif" alt="Variant Swap Showcase" width="500">

### **Customization**
The mod's generated mappings and configuration file are located in your Minecraft (or server) folder under `/config/Variant Swap/`.

Available settings in `config.json`:
- OP level (4 by default) - for the `/variant-swap` command
- Delay (50ms by default) - this is to throttle the scrolling

On servers it is recommended to change the OP level as desired. The `/variant-swap` command can be used to reset the delay (cooldown) to its default value or to change it to another value (still in `ms`).

### **For Developers**
If your mod adds items or blocks and you want to organize them into specific custom categories, add a `variant_swap` tag to each item/block. This tag will determine the group in which the item is categorized.

For example, imagine you have a mod that adds various furniture items, and you want to categorize them by wood type instead of grouping all chairs together. You could tag all oak items with `variant_swap:oak` (so `oak` or whatever you wanna call the category), all spruce items with `variant_swap:spruce`, and so on.
