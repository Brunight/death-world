# ☠️ Death World

This plugin will create a Lobby world with a bedrock platform. Then, an admin can begin the gameplay by running `/dw start` to create a new survival world and teleport everyone there. If someone dies, the whole world will be **fully deleted**, simple as that! Everyone is teleported back to the Lobby, the world will be deleted **with no chance for backups**, a new survival world will be created and so on!

Players death count will be in their nicknames and tab list, and players death messages will be stored in the plugin data folder.

## Modes

### `default`
In this mode, after a player dies, a new world will be generated and gameplay will restart there.

### `killall`
In this mode, after a player dies, everyone is also killed, and the current world progress is kept.\
Players that were offline when a death happened will be killed upon joining the server (it will not be counted to trigger a death).

### `none`
In this mode, after a player dies, nothing is done.\
Deaths will not be counted. No death messages are sent. Player will "vanilla-die", that is, will lose items and EXP. 

## Commands

### `/dw start`
This command will delete the ongoing survival world, create a new one, and teleport everyone there.

### `/dw softkill <player>`
This command will softkill a player.\
This means that the target player will die, but will not trigger a world death and will not be counted.

## Configuration

### `mode` - string

- `default` will create a new world on player death;
- `killall` will kill everyone, but current world and progress is kept.
- `none` will do nothing, and won't track deaths.

See more details about modes above. Defaults to `default`.

### `autoGenerateNewWorld` - boolean

This option defines if a new world should be automatically generated on player death. If set to false, an admin needs to run the start command to create a new world and continue the gameplay. This should give time to your players to mock the player who died! Only works in `default` mode. Defaults to `true`.