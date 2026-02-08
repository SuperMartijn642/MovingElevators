### Moving Elevators 1.4.12
- Added feedback when cabins contain an invalid block or when there are blocks obstructing the destination
- Elevator now decelerates when approaching its destination floor
- Adjusted elevator display layout
- Cabin availability is now lazily cached and shown on elevator displays
- Added config option to allow moving unbreakable blocks
- Increased default elevator size limit to 11 blocks
- Improved elevator rendering for blocks with complex models
- Fixed cabin check when the cabin area of floors overlaps
- Fixed camo data serialization when blocks are added/removed from the game

### Moving Elevators 1.4.11
- Added Turkish translations (thanks to RuyaSavascisi!)
- Fixed entities being ridden by the player sometimes taking damage when the elevator arrives
- Fixed crash when dropping elevator cabin blocks as items
- Fixed crash when rendering elevator with Sodium Extra installed

### Moving Elevators 1.4.10
- Elevator systems will now validate saved controller positions when the chunk they are in gets loaded
- Added Japanese translations (thanks to Konuma Takaki!)
- Added Korean translations (thanks to park-cheolwoo!)

### Moving Elevators 1.4.9
- Fixed issues with block updates when removing/placing elevator cabin

### Moving Elevators 1.4.8
- Improved compatibility with Embeddium, Iris, Oculus, Rubidium, and Sodium
- Fixed elevator placing random blocks instead of air when mods shift vanilla block ids
- Fixed some blocks like doors not being updated after being moved with the elevator

### Moving Elevators 1.4.7
- Updated Russian and Ukrainian translations (thanks to dnrovs!)
- Render distance for moving elevators is now based on actual render distance rather than fixed 300 blocks
- Small optimizations when rendering elevators

### Moving Elevators 1.4.6
- Light blocks will now be treated as air for elevator cabin checks
- Fixed rare crash when calculating light value for elevator blocks
- Fixed crash when removing a floor right after remote elevator panel caching
- Added Simplified Chinese and Traditional Chinese translation (thanks to SheepYhangCN!)
- Fixed some of the French translations (thanks to OptimusZeGaming!)

### Moving Elevators 1.4.5
- Initial release of Moving Elevators for Minecraft 1.20.3 & 1.20.4
