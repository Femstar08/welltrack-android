import { Avatar, AvatarFallback, AvatarImage } from "./ui/avatar";
import { ChevronDown, Users } from "lucide-react";
import { ThemeToggle } from "./ThemeToggle";

export function Header() {
  const currentDate = new Date().toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });

  return (
    <header className="flex items-center justify-between p-4 bg-gradient-to-r from-green-500 to-emerald-600 text-white dark:from-green-600 dark:to-emerald-700">
      <div className="flex items-center space-x-3">
        <div className="w-8 h-8 bg-white rounded-lg flex items-center justify-center">
          <span className="text-green-600 font-bold text-lg">W</span>
        </div>
        <div>
          <h1 className="text-xl font-semibold">WellTrack</h1>
          <p className="text-green-100 text-sm">{currentDate}</p>
        </div>
      </div>
      
      <div className="flex items-center space-x-3">
        <ThemeToggle />
        <div className="flex items-center space-x-2 bg-white/20 rounded-full px-3 py-1">
          <Users className="w-4 h-4" />
          <ChevronDown className="w-4 h-4" />
        </div>
        <Avatar className="w-10 h-10 border-2 border-white/30">
          <AvatarImage src="https://images.unsplash.com/photo-1494790108755-2616b612b8c4?w=100&h=100&fit=crop&crop=face" />
          <AvatarFallback className="bg-green-700 text-white">JD</AvatarFallback>
        </Avatar>
      </div>
    </header>
  );
}