import { Button } from "./ui/button";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "./ui/dropdown-menu";
import { ChevronLeft, ChevronRight, Users, Settings } from "lucide-react";

interface MealPlannerHeaderProps {
  currentWeek: Date;
  onWeekChange: (direction: "prev" | "next") => void;
  selectedUser: string;
  onUserChange: (user: string) => void;
}

export function MealPlannerHeader({ 
  currentWeek, 
  onWeekChange, 
  selectedUser, 
  onUserChange 
}: MealPlannerHeaderProps) {
  const getWeekRange = (date: Date) => {
    const startOfWeek = new Date(date);
    const day = startOfWeek.getDay();
    const diff = startOfWeek.getDate() - day + (day === 0 ? -6 : 1); // Adjust for Monday start
    startOfWeek.setDate(diff);
    
    const endOfWeek = new Date(startOfWeek);
    endOfWeek.setDate(startOfWeek.getDate() + 6);
    
    return {
      start: startOfWeek.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
      end: endOfWeek.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
    };
  };

  const weekRange = getWeekRange(currentWeek);
  const users = [
    { id: "current", name: "My Plan", avatar: "https://images.unsplash.com/photo-1494790108755-2616b612b8c4?w=32&h=32&fit=crop&crop=face" },
    { id: "family", name: "Family Plan", avatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=32&h=32&fit=crop&crop=face" },
    { id: "kids", name: "Kids Only", avatar: "https://images.unsplash.com/photo-1544723795-3fb6469f5b39?w=32&h=32&fit=crop&crop=face" }
  ];

  const currentUser = users.find(u => u.id === selectedUser) || users[0];

  return (
    <div className="bg-card border-b border-border px-4 py-3">
      <div className="flex items-center justify-between mb-3">
        <h1 className="text-xl font-semibold text-card-foreground">Meal Planner</h1>
        <div className="flex items-center space-x-2">
          {/* User Switcher */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="sm" className="h-8 gap-2">
                <img 
                  src={currentUser.avatar} 
                  alt={currentUser.name}
                  className="w-6 h-6 rounded-full"
                />
                <span className="text-sm">{currentUser.name}</span>
                <Users className="w-4 h-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              {users.map((user) => (
                <DropdownMenuItem
                  key={user.id}
                  onClick={() => onUserChange(user.id)}
                  className={`gap-2 ${selectedUser === user.id ? 'bg-accent' : ''}`}
                >
                  <img 
                    src={user.avatar} 
                    alt={user.name}
                    className="w-5 h-5 rounded-full"
                  />
                  {user.name}
                </DropdownMenuItem>
              ))}
            </DropdownMenuContent>
          </DropdownMenu>

          {/* Settings */}
          <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
            <Settings className="w-4 h-4" />
          </Button>
        </div>
      </div>

      {/* Week Navigation */}
      <div className="flex items-center justify-between">
        <Button 
          variant="ghost" 
          size="sm" 
          onClick={() => onWeekChange("prev")}
          className="h-8 w-8 p-0"
        >
          <ChevronLeft className="w-5 h-5" />
        </Button>
        
        <div className="text-center">
          <p className="text-base font-medium text-card-foreground">
            {weekRange.start} - {weekRange.end}
          </p>
          <p className="text-xs text-muted-foreground">
            {currentWeek.getFullYear()}
          </p>
        </div>

        <Button 
          variant="ghost" 
          size="sm" 
          onClick={() => onWeekChange("next")}
          className="h-8 w-8 p-0"
        >
          <ChevronRight className="w-5 h-5" />
        </Button>
      </div>
    </div>
  );
}