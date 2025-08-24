import { useState } from "react";
import { MealPlannerHeader } from "./MealPlannerHeader";
import { WeeklyCalendar } from "./WeeklyCalendar";
import { QuickActionsBar } from "./QuickActionsBar";
import { SmartSuggestions } from "./SmartSuggestions";

export function MealPlanner() {
  const [currentWeek, setCurrentWeek] = useState(new Date());
  const [selectedUser, setSelectedUser] = useState("current");
  const [mealPrepMode, setMealPrepMode] = useState(false);
  const [showSuggestions, setShowSuggestions] = useState(false);

  const handleWeekChange = (direction: "prev" | "next") => {
    setCurrentWeek(prev => {
      const newDate = new Date(prev);
      newDate.setDate(newDate.getDate() + (direction === "next" ? 7 : -7));
      return newDate;
    });
  };

  return (
    <div className="flex flex-col h-full">
      {/* Header Section */}
      <MealPlannerHeader 
        currentWeek={currentWeek}
        onWeekChange={handleWeekChange}
        selectedUser={selectedUser}
        onUserChange={setSelectedUser}
      />

      {/* Quick Actions Bar */}
      <QuickActionsBar
        mealPrepMode={mealPrepMode}
        onMealPrepModeChange={setMealPrepMode}
        onShowSuggestions={() => setShowSuggestions(!showSuggestions)}
        showSuggestions={showSuggestions}
      />

      {/* Main Content */}
      <div className="flex-1 overflow-y-auto">
        <WeeklyCalendar 
          currentWeek={currentWeek}
          mealPrepMode={mealPrepMode}
        />

        {/* Mobile Smart Suggestions - Show as overlay on mobile when sidebar is not available */}
        {showSuggestions && (
          <div className="md:hidden fixed inset-0 z-50 bg-background">
            <div className="flex flex-col h-full">
              <div className="p-4 border-b border-border">
                <button
                  onClick={() => setShowSuggestions(false)}
                  className="text-sm text-muted-foreground"
                >
                  ← Back to Calendar
                </button>
              </div>
              <div className="flex-1 overflow-y-auto">
                <SmartSuggestions />
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}