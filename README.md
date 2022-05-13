# MinestomBlocks

[![license](https://img.shields.io/github/license/TogAr2/MinestomBlocks.svg?style=flat-square)](LICENSE)

MinestomBlocks is a library for Minestom. It adds block sounds (e.g., placement and step) and block breaking animation.

The maven repository is available on [jitpack](https://jitpack.io/#TogAr2/MinestomBlocks).

## Table of Contents

- [Features](#features)

## Features

Currently, the library provides the following functionality:
- Block placement sounds
- Player walking sounds
- Player falling sounds
- Block breaking animation including support for different tools, efficiency, haste and mining fatigue

## Usage

To use the library, add it as a dependency to your project.

Before using it, you should call `MinestomBlocks.init()`. This will make sure everything is registered correctly.
After you've initialized the extension, you can get an `EventNode` containing listeners which provide specific
functionalities using the static methods in `MinestomBlocks`. To get all the features, use `MinestomBlocks.events()`.
By adding these nodes as a child to any other node, you enable the feature in that scope.

Example:
```java
MinestomBlocks.init();
MinecraftServer.getGlobalEventHandler().addChild(MinestomBlocks.events());
```

### Integration

MinestomBlocks uses a custom listener for the `ClientPlayerDiggingPacket`.
In case you want to implement your own breaking functionality, you should use `PlayerDiggingActionEvent`,
which provides you with a method to set the handler.

If for whatever reason you want to access the `BlockSoundGroup` of a block, you can do so using `MinestomBlocks.getSoundGroup(block)`.

## How it works

MinestomBlocks uses [ArticData](https://github.com/Articdive/ArticData) to generate a file which contains information about blocks.
Upon calling `MinestomBlocks.init()`, the library will read and parse the file.
It stores information about which `BlockSoundGroup` each block belongs to.

## Contributing

Pull requests are welcome! (If within the scope of this project)
