import { Header } from "./components/Header";
import { MealCard } from "./components/MealCard";
import { NutritionCard } from "./components/NutritionCard";
import { QuickActions } from "./components/QuickActions";
import { HealthInsights } from "./components/HealthInsights";
import { BottomNavigation } from "./components/BottomNavigation";
import { MealPlanner } from "./components/MealPlanner";
import { ShoppingList } from "./components/ShoppingList";
import { MealPrepStorage } from "./components/MealPrepStorage";
import { HealthAnalytics } from "./components/HealthAnalytics";
import { Supplements } from "./components/Supplements";
import { Recipes } from "./components/Recipes";
import { Profile } from "./components/Profile";
import { Biomarkers } from "./components/Biomarkers";
import { ThemeProvider } from "./components/ThemeProvider";
import { SidebarProvider, Sidebar, SidebarInset, SidebarTrigger } from "./components/ui/sidebar";
import { AppSidebar } from "./components/AppSidebar";
import { Calendar, BookOpen, ChefHat, ShoppingCart, BarChart3, Activity, Pill, User } from "lucide-react";
import { useState } from "react";

export default function App() {
  const [currentScreen, setCurrentScreen] = useState("dashboard");
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // Screens that should have a sidebar
  const screensWithSidebar = ["meal-planner", "recipes", "health-analytics", "biomarkers"];
  const hasSidebar = screensWithSidebar.includes(currentScreen);

  const renderScreen = () => {
    switch (currentScreen) {
      case "meal-planner":
        return <MealPlanner />;
      case "shopping":
        return <ShoppingList />;
      case "meal-prep":
        return <MealPrepStorage />;
      case "health-analytics":
        return <HealthAnalytics />;
      case "supplements":
        return <Supplements />;
      case "recipes":
        return <Recipes />;
      case "profile":
        return <Profile />;
      case "biomarkers":
        return <Biomarkers />;
      case "dashboard":
      default:
        return (
          <div className="max-w-md mx-auto p-4 space-y-6">
            {/* Today's Summary Section */}
            <section className="space-y-4">
              <h2 className="text-lg font-semibold text-foreground">Today's Summary</h2>
              <MealCard />
              <NutritionCard />
            </section>
            
            {/* Quick Actions Section */}
            <section className="space-y-4">
              <h2 className="text-lg font-semibold text-foreground">Quick Actions</h2>
              <QuickActions onNavigate={setCurrentScreen} />
            </section>
            
            {/* All Apps Overview */}
            <section className="space-y-4">
              <h2 className="text-lg font-semibold text-foreground">All Features</h2>
              <div className="grid grid-cols-2 gap-3">
                <button 
                  onClick={() => setCurrentScreen("meal-planner")}
                  className="flex flex-col items-center gap-2 p-4 rounded-lg border border-border hover:bg-accent transition-colors"
                >
                  <Calendar className="w-6 h-6 text-blue-500" />
                  <span className="text-sm font-medium">Meal Planner</span>
                  <span className="text-xs text-muted-foreground text-center">Plan weekly meals with smart suggestions</span>
                </button>
                
                <button 
                  onClick={() => setCurrentScreen("recipes")}
                  className="flex flex-col items-center gap-2 p-4 rounded-lg border border-border hover:bg-accent transition-colors"
                >
                  <BookOpen className="w-6 h-6 text-orange-500" />
                  <span className="text-sm font-medium">Recipes</span>
                  <span className="text-xs text-muted-foreground text-center">Discover & save healthy recipes</span>
                </button>
                
                <button 
                  onClick={() => setCurrentScreen("meal-prep")}
                  className="flex flex-col items-center gap-2 p-4 rounded-lg border border-border hover:bg-accent transition-colors"
                >
                  <ChefHat className="w-6 h-6 text-purple-500" />
                  <span className="text-sm font-medium">Meal Prep</span>
                  <span className="text-xs text-muted-foreground text-center">Batch cooking & storage</span>
                </button>
                
                <button 
                  onClick={() => setCurrentScreen("shopping")}
                  className="flex flex-col items-center gap-2 p-4 rounded-lg border border-border hover:bg-accent transition-colors"
                >
                  <ShoppingCart className="w-6 h-6 text-green-500" />
                  <span className="text-sm font-medium">Shopping</span>
                  <span className="text-xs text-muted-foreground text-center">Smart grocery lists</span>
                </button>
                
                <button 
                  onClick={() => setCurrentScreen("health-analytics")}
                  className="flex flex-col items-center gap-2 p-4 rounded-lg border border-border hover:bg-accent transition-colors"
                >
                  <BarChart3 className="w-6 h-6 text-yellow-500" />
                  <span className="text-sm font-medium">Health Analytics</span>
                  <span className="text-xs text-muted-foreground text-center">Track health & fitness</span>
                </button>
                
                <button 
                  onClick={() => setCurrentScreen("biomarkers")}
                  className="flex flex-col items-center gap-2 p-4 rounded-lg border border-border hover:bg-accent transition-colors"
                >
                  <Activity className="w-6 h-6 text-red-500" />
                  <span className="text-sm font-medium">Biomarkers</span>
                  <span className="text-xs text-muted-foreground text-center">Blood test tracking</span>
                </button>
                
                <button 
                  onClick={() => setCurrentScreen("supplements")}
                  className="flex flex-col items-center gap-2 p-4 rounded-lg border border-border hover:bg-accent transition-colors"
                >
                  <Pill className="w-6 h-6 text-blue-500" />
                  <span className="text-sm font-medium">Supplements</span>
                  <span className="text-xs text-muted-foreground text-center">Supplement tracking</span>
                </button>
                
                <button 
                  onClick={() => setCurrentScreen("profile")}
                  className="flex flex-col items-center gap-2 p-4 rounded-lg border border-border hover:bg-accent transition-colors"
                >
                  <User className="w-6 h-6 text-gray-500" />
                  <span className="text-sm font-medium">Profile</span>
                  <span className="text-xs text-muted-foreground text-center">Settings & preferences</span>
                </button>
              </div>
            </section>
            
            {/* Health Insights Section */}
            <section className="space-y-4">
              <h2 className="text-lg font-semibold text-foreground">Health Insights</h2>
              <HealthInsights />
            </section>
          </div>
        );
    }
  };

  return (
    <ThemeProvider>
      <div className="min-h-screen bg-background flex flex-col transition-colors">
        {hasSidebar ? (
          <SidebarProvider open={sidebarOpen} onOpenChange={setSidebarOpen}>
            <div className="flex flex-1">
              <AppSidebar 
                currentScreen={currentScreen}
                onNavigate={setCurrentScreen}
              />
              <SidebarInset className="flex flex-col">
                {/* Header with Sidebar Toggle */}
                <div className="border-b border-border bg-gradient-to-r from-green-500 to-emerald-600 text-white dark:from-green-600 dark:to-emerald-700">
                  <div className="flex items-center justify-between p-4">
                    <div className="flex items-center gap-3">
                      <SidebarTrigger className="bg-white/20 hover:bg-white/30 text-white border-none" />
                      <div className="flex items-center space-x-3">
                        <div className="w-8 h-8 bg-white rounded-lg flex items-center justify-center">
                          <span className="text-green-600 font-bold text-lg">W</span>
                        </div>
                        <div>
                          <h1 className="text-xl font-semibold">WellTrack</h1>
                          <p className="text-green-100 text-sm capitalize">
                            {currentScreen.replace('-', ' ')}
                          </p>
                        </div>
                      </div>
                    </div>
                    
                    <div className="flex items-center space-x-3">
                      <div className="flex items-center space-x-2 bg-white/20 rounded-full px-3 py-1">
                        <span className="text-sm">Family Mode</span>
                      </div>
                    </div>
                  </div>
                </div>
                
                {/* Main Content */}
                <main className="flex-1 overflow-y-auto pb-20">
                  {renderScreen()}
                </main>
                
                {/* Bottom Navigation */}
                <BottomNavigation currentScreen={currentScreen} onScreenChange={setCurrentScreen} />
              </SidebarInset>
            </div>
          </SidebarProvider>
        ) : (
          <>
            {/* Header */}
            <Header />
            
            {/* Main Content */}
            <main className="flex-1 overflow-y-auto pb-20">
              {renderScreen()}
            </main>
            
            {/* Bottom Navigation */}
            <BottomNavigation currentScreen={currentScreen} onScreenChange={setCurrentScreen} />
          </>
        )}
      </div>
    </ThemeProvider>
  );
}