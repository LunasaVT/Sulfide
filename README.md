## Sulfide

Fast rendering engine for Minecraft 1.8.9, because the GPU is there to be used.

### Features
* Instanced Entity Rendering - Fast, per-model batching for all entities. Significantly improves performance while not losing out on visual quality.
* Fast Sky Rendering - Pretty self explanatory. Improved, re-written sky renderer.
* Fast Cloud Rendering - Also pretty self explanatory. Improved, re-written cloud renderer.
* GPU Lightmap Compute - Optionally uses a compute shader to do lightmap calculations. The GPU is there, so why not use it?

### Compatibility

Good news - Sulfide is compatible with mostly everything, AS LONG AS it doesn't modify model part rendering. If a mod modifies that... well we wouldn't have much hopes.

As for operating systems... well, this is incompatible with macOS. Should work on Linux and Windows. You need Radium (or any other mod that provides LWJGL3) to use Sulfide, so OptiFine is out the window.

### Need Help?

We got a [Discord](https://discord.gg/cdjBUc5JHR) for that.

### FAQ

**Q**: Will this work with my 2012 laptop?

**A**: If it has a GPU made this decade, probably! Compute shaders need some modern-ish hardware though. Intel HD 4000 users, we salute your courage.

_TL;DR: If it supports GL 4.3, Sulfide will run!_

**Q**: Why 1.8.9?

**A**: Because PvP players are a stubborn bunch and we love them for it, it's also my personal favorite version. Also, the code was right there begging for optimization.

### License

Do whatever, just don't sell it as your own. If you make a ton of money, buy us coffee. Or a GPU. We're not picky.

On a more serious note, Sulfide is licensed under LGPL v3.