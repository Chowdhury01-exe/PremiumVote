Possible fix for the GUI not opening:

1) Register the listener once (onEnable):
        ----------------------------------------
StartElectionGUI startGui = new StartElectionGUI(manager, this, signInput);
Bukkit.getPluginManager().registerEvents(startGui, this);
// If SetElectionNameGUI / SignInput are Listeners too, register them as well.

2) Open inventories/sign on the next tick (don’t open inside the same click tick):
Possible fix for the GUI not opening:

        1) Register the listener once (onEnable):
        ----------------------------------------
StartElectionGUI startGui = new StartElectionGUI(manager, this, signInput);
Bukkit.getPluginManager().registerEvents(startGui, this);
// If SetElectionNameGUI / SignInput are Listeners too, register them as well.

2) Open inventories/sign on the next tick (don’t open inside the same click tick):
        ----------------------------------------------------------------------------------
        case "Set Duration":
        Bukkit.getScheduler().runTask(plugin, () -> openDurationGUI(player));//duration GUI working fine
        break;

        case "Set Election Name":
        Bukkit.getScheduler().runTask(plugin, () -> setElectionNameGUI.open(player));
        break;

        3) Guard against bottom-inventory clicks + null display names:
        --------------------------------------------------------------
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName());

        4) (Optional) Avoid leaks: use UUID for setupMap and clean up on close/quit:
        ----------------------------------------------------------------------------
        private final Map<UUID, ElectionSetup> setupMap = new HashMap<>();

        @EventHandler
        public void onClose(InventoryCloseEvent e) {
        String t = e.getView().getTitle();
        if (t.equals(ChatColor.GREEN + ":ballot_box: Start Election") || t.equals(ChatColor.AQUA + ":hourglass_flowing_sand: Set Duration")) {
        setupMap.remove(((Player)e.getPlayer()).getUniqueId());
        }
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent e) {
        setupMap.remove(e.getPlayer().getUniqueId());
        }

        5) Quick sanity test:
        ---------------------
        If this doesn't open from a simple command:
        player.openInventory(Bukkit.createInventory(null, 9, "Test"));
        …then it's a registration/async issue. If it does open, the next-tick scheduling above should fix it.

        Commit message:
        ---------------
        Fix GUI not opening: register listeners, schedule inventory opens next tick, add null/slot guards, use UUID map.
