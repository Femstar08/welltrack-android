import { useState } from "react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./ui/tabs";
import { MealPrepHeader } from "./MealPrepHeader";
import { ActivePrepSession } from "./ActivePrepSession";
import { PrepPlanningSection } from "./PrepPlanningSection";
import { StorageManagement } from "./StorageManagement";
import { LeftoverManagement } from "./LeftoverManagement";

export interface PrepSession {
  id: string;
  recipeName: string;
  startTime: Date;
  estimatedDuration: number;
  currentStep: number;
  totalSteps: number;
  isActive: boolean;
}

export interface StoredMeal {
  id: string;
  name: string;
  prepDate: Date;
  expiryDate: Date;
  location: 'fridge' | 'freezer' | 'pantry';
  containerType: string;
  portions: number;
  remainingPortions: number;
  reheatingInstructions: string;
  image?: string;
  freshness: 'fresh' | 'good' | 'use-soon' | 'expired';
}

export interface LeftoverItem {
  id: string;
  name: string;
  quantity: string;
  expiryDate: Date;
  location: 'fridge' | 'freezer';
  freshness: 'fresh' | 'good' | 'use-soon' | 'expired';
  suggestions: string[];
}

export function MealPrepStorage() {
  const [activeTab, setActiveTab] = useState("planning");
  const [activeSession, setActiveSession] = useState<PrepSession | null>(null);
  const [weekView, setWeekView] = useState(false);

  // Mock active session for demonstration
  const mockActiveSession: PrepSession = {
    id: 'session-1',
    recipeName: 'Quinoa Buddha Bowls (4 portions)',
    startTime: new Date(Date.now() - 25 * 60 * 1000), // Started 25 minutes ago
    estimatedDuration: 45,
    currentStep: 3,
    totalSteps: 8,
    isActive: true
  };

  const hasActiveSession = activeSession || mockActiveSession;

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <MealPrepHeader 
        activeSession={hasActiveSession}
        weekView={weekView}
        onWeekViewToggle={setWeekView}
      />

      {/* Active Prep Session - Shows when user is actively prepping */}
      {hasActiveSession && (
        <ActivePrepSession 
          session={hasActiveSession}
          onComplete={() => setActiveSession(null)}
          onPause={() => setActiveSession(null)}
        />
      )}

      {/* Main Tabbed Content */}
      <div className="flex-1 overflow-hidden">
        <Tabs value={activeTab} onValueChange={setActiveTab} className="h-full flex flex-col">
          <TabsList className="grid w-full grid-cols-4 mx-4 mt-2">
            <TabsTrigger value="planning" className="text-xs">Planning</TabsTrigger>
            <TabsTrigger value="storage" className="text-xs">Storage</TabsTrigger>
            <TabsTrigger value="leftovers" className="text-xs">Leftovers</TabsTrigger>
            <TabsTrigger value="guidance" className="text-xs">Guidance</TabsTrigger>
          </TabsList>

          <div className="flex-1 overflow-hidden">
            <TabsContent value="planning" className="h-full m-0">
              <PrepPlanningSection weekView={weekView} />
            </TabsContent>

            <TabsContent value="storage" className="h-full m-0">
              <StorageManagement />
            </TabsContent>

            <TabsContent value="leftovers" className="h-full m-0">
              <LeftoverManagement />
            </TabsContent>

            <TabsContent value="guidance" className="h-full m-0">
              <div className="p-4 space-y-4">
                <h3 className="font-medium text-card-foreground">Storage Guidance</h3>
                <p className="text-sm text-muted-foreground">Storage tips and guidelines coming soon...</p>
              </div>
            </TabsContent>
          </div>
        </Tabs>
      </div>
    </div>
  );
}