Siege Lifecycle

Starting a siege on a territory returns true -> Done

Starting a siege on an already sieged territory returns false -> Done

Ending a siege on a territory returns true -> Done

Ending a siege on a territory with no siege returns false -> Done

Getting a siege by territory ID returns non-null if active -> Done

Getting a siege by territory ID returns null if inactive -> Done

Checking if a territory is under siege returns true/false correctly -> Done

Teams (Inside a Siege)

Adding a player to the attacker team succeeds -> Done

Adding a player to the defender team succeeds -> Done


Adding the same player to two teams fails -> Done

Getting a player’s team returns the correct team -> Not needed

Checking if two players are on the same team returns true/false correctly -> Done

Checking if a player is in the siege returns true/false correctly  -> Done

Points

Adding banner capture points increases team points -> Hytale integration
Adding kill points increases team points -> Hytale integration

Adding point to a team -> Done

Getting team total points returns the correct value -> Done

Comparing two teams’ points returns the correct winner -> true

Timer & State

Starting the siege timer sets the start time

Siege reports expired when duration is exceeded

Siege reports not expired before duration

Setting banner active marks banner as active

Setting banner inactive marks banner as inactive

Checking banner state returns correct value

Outcome

Determining winner returns attacker team when attacker has more points -> Done

Determining winner returns defender team when defender has more points -> Done

Determining winner returns no winner on tie -> Done

Territory Effects

Setting territory owner updates owner correctly

Getting territory owner returns correct owner

Setting territory color updates color correctly

Setting territory tax rate updates tax rate correctly

Validation

Siege cannot start on invalid territory

Banner cannot be placed if territory is already sieged

Player cannot interact with siege if not in active siege