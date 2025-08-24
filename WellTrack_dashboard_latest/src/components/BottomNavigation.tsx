import { Home, Calendar, ChefHat, Pill, BookOpen, BarChart3, User, ShoppingCart } from "lucide-react";

interface NavItem {
  id: string;
  label: string;
  icon: React.ReactNode;
}

interface BottomNavigationProps {
  currentScreen: string;
  onScreenChange: (screen: string) => void;
}

export function BottomNavigation({ currentScreen, onScreenChange }: BottomNavigationProps) {
  const navItems: NavItem[] = [
    {
      id: "dashboard",
      label: "Dashboard",
      icon: <Home className="w-5 h-5" />
    },
    {
      id: "meal-planner",
      label: "Planner",
      icon: <Calendar className="w-5 h-5" />
    },
    {
      id: "meal-prep",
      label: "Meal Prep",
      icon: <ChefHat className="w-5 h-5" />
    },
    {
      id: "shopping",
      label: "Shopping",
      icon: <ShoppingCart className="w-5 h-5" />
    },
    {
      id: "profile",
      label: "Profile",
      icon: <User className="w-5 h-5" />
    }
  ];

  return (
    <nav className="bg-card border-t border-border px-4 py-2 fixed bottom-0 left-0 right-0 z-50">
      <div className="flex justify-around max-w-md mx-auto">
        {navItems.map((item) => (
          <button
            key={item.id}
            onClick={() => onScreenChange(item.id)}
            className={`flex flex-col items-center space-y-1 py-2 px-3 rounded-lg transition-colors min-h-[48px] min-w-[48px] ${
              currentScreen === item.id
                ? "text-green-600 bg-green-50 dark:bg-green-950 dark:text-green-400"
                : "text-muted-foreground hover:text-foreground hover:bg-accent"
            }`}
          >
            {item.icon}
            <span className="text-xs">{item.label}</span>
          </button>
        ))}
      </div>
    </nav>
  );
}