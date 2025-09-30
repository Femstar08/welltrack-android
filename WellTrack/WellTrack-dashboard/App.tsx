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
import { ThemeProvider } from "./components/ThemeProvider";
import { useState } from "react";

export default function App() {
  const [currentScreen, setCurrentScreen] = useState("dashboard");

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
              <QuickActions />
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
        {/* Header */}
        <Header />
        
        {/* Main Content */}
        <main className="flex-1 overflow-y-auto pb-20">
          {renderScreen()}
        </main>
        
        {/* Bottom Navigation */}
        <BottomNavigation currentScreen={currentScreen} onScreenChange={setCurrentScreen} />
      </div>
    </ThemeProvider>
  );
}