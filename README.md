# ☠️ Death World

This plugin will create a Lobby world with a bedrock platform. Then, an admin can begin the gameplay by running `/dw start` to create a new survival world and teleport everyone there. If someone dies, the whole world will be **fully deleted**, simple as that! Everyone is teleported back to the Lobby, the world will be deleted **with no chance for backups**, a new survival world will be created and so on!

Players death count will be in their nicknames and tab list, and players death messages will be stored in the plugin data folder.

## Commands

### `/dw start`
This command will delete the ongoing survival world, create a new one, and teleport everyone there.

## Configuration

### String `mode`

- `default` will create a new world on player death;
- `killall` will kill everyone, but current world and progress is kept.
Defaults to `default`.

### Boolean `autoGenerateNewWorld`

This config defines if a new world should be automatically generated on player death. If set to false, an admin needs to run the start command to create a new world and continue the gameplay. This gives players time to mock the player who died! Only works with 'mode' = 'default'. Default to `true`.