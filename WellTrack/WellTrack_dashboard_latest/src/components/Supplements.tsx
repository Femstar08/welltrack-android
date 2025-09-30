import { useState } from "react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./ui/tabs";
import { SupplementsHeader } from "./SupplementsHeader";
import { TodaySchedule } from "./TodaySchedule";
import { SupplementLibrary } from "./SupplementLibrary";
import { SupplementAnalytics } from "./SupplementAnalytics";
import { SmartRecommendations } from "./SmartRecommendations";

export interface Supplement {
  id: string;
  name: string;
  brand: string;
  form: 'tablet' | 'capsule' | 'powder' | 'liquid' | 'gummy';
  dosage: number;
  unit: string;
  purpose: string;
  stockLevel: number;
  totalStock: number;
  expiryDate: string;
  cost: number;
  effectiveness: number;
  sideEffects?: string;
  interactions?: string[];
  category: 'vitamin' | 'mineral' | 'herb' | 'amino' | 'omega' | 'probiotic' | 'other';
}

export interface SupplementSchedule {
  id: string;
  supplementId: string;
  timeSlots: {
    morning?: boolean;
    preWorkout?: boolean;
    afternoon?: boolean;
    evening?: boolean;
    bedtime?: boolean;
  };
  frequency: 'daily' | 'weekly' | 'asNeeded';
  withFood: boolean;
  customTime?: string;
  flexibilityWindow: number; // minutes
  active: boolean;
}

export interface SupplementIntake {
  id: string;
  supplementId: string;
  scheduledTime: string;
  actualTime?: string;
  status: 'taken' | 'missed' | 'skipped';
  dosage: number;
  notes?: string;
  date: string;
}

export function Supplements() {
  const [activeTab, setActiveTab] = useState("today");
  const [currentTime] = useState(new Date());

  // Mock supplement data
  const supplements: Supplement[] = [
    {
      id: '1',
      name: 'Vitamin D3',
      brand: 'Nature Made',
      form: 'capsule',
      dosage: 2000,
      unit: 'IU',
      purpose: 'Bone health & immunity',
      stockLevel: 45,
      totalStock: 60,
      expiryDate: '2025-06-15',
      cost: 0.25,
      effectiveness: 4,
      category: 'vitamin'
    },
    {
      id: '2',
      name: 'Omega-3 Fish Oil',
      brand: 'Nordic Naturals',
      form: 'capsule',
      dosage: 1000,
      unit: 'mg',
      purpose: 'Heart & brain health',
      stockLevel: 28,
      totalStock: 60,
      expiryDate: '2025-03-20',
      cost: 0.50,
      effectiveness: 5,
      category: 'omega'
    },
    {
      id: '3',
      name: 'Magnesium Glycinate',
      brand: 'Thorne',
      form: 'capsule',
      dosage: 200,
      unit: 'mg',
      purpose: 'Sleep & muscle recovery',
      stockLevel: 52,
      totalStock: 60,
      expiryDate: '2025-08-10',
      cost: 0.35,
      effectiveness: 4,
      category: 'mineral'
    },
    {
      id: '4',
      name: 'Whey Protein',
      brand: 'Optimum Nutrition',
      form: 'powder',
      dosage: 30,
      unit: 'g',
      purpose: 'Post-workout recovery',
      stockLevel: 15,
      totalStock: 30,
      expiryDate: '2025-05-01',
      cost: 1.20,
      effectiveness: 5,
      category: 'amino'
    },
    {
      id: '5',
      name: 'B-Complex',
      brand: 'Garden of Life',
      form: 'capsule',
      dosage: 1,
      unit: 'capsule',
      purpose: 'Energy & metabolism',
      stockLevel: 38,
      totalStock: 60,
      expiryDate: '2025-04-12',
      cost: 0.30,
      effectiveness: 4,
      category: 'vitamin'
    }
  ];

  // Mock schedule data
  const schedules: SupplementSchedule[] = [
    {
      id: '1',
      supplementId: '1',
      timeSlots: { morning: true },
      frequency: 'daily',
      withFood: true,
      flexibilityWindow: 60,
      active: true
    },
    {
      id: '2',
      supplementId: '2',
      timeSlots: { morning: true, evening: true },
      frequency: 'daily',
      withFood: true,
      flexibilityWindow: 30,
      active: true
    },
    {
      id: '3',
      supplementId: '3',
      timeSlots: { bedtime: true },
      frequency: 'daily',
      withFood: false,
      flexibilityWindow: 30,
      active: true
    },
    {
      id: '4',
      supplementId: '4',
      timeSlots: { preWorkout: true },
      frequency: 'daily',
      withFood: false,
      flexibilityWindow: 15,
      active: true
    },
    {
      id: '5',
      supplementId: '5',
      timeSlots: { morning: true },
      frequency: 'daily',
      withFood: true,
      flexibilityWindow: 45,
      active: true
    }
  ];

  // Mock today's intake data
  const todayIntakes: SupplementIntake[] = [
    {
      id: '1',
      supplementId: '1',
      scheduledTime: '08:00',
      actualTime: '08:15',
      status: 'taken',
      dosage: 2000,
      date: '2024-11-18'
    },
    {
      id: '2',
      supplementId: '2',
      scheduledTime: '08:00',
      actualTime: '08:15',
      status: 'taken',
      dosage: 1000,
      date: '2024-11-18'
    },
    {
      id: '3',
      supplementId: '5',
      scheduledTime: '08:00',
      status: 'missed',
      dosage: 1,
      date: '2024-11-18'
    },
    {
      id: '4',
      supplementId: '4',
      scheduledTime: '16:00',
      status: 'taken',
      actualTime: '16:30',
      dosage: 30,
      date: '2024-11-18'
    }
  ];

  // Calculate adherence
  const totalScheduled = schedules.length * 2; // Assuming morning/evening for simplicity
  const takenToday = todayIntakes.filter(intake => intake.status === 'taken').length;
  const adherencePercentage = Math.round((takenToday / totalScheduled) * 100);

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <SupplementsHeader 
        adherencePercentage={adherencePercentage}
        takenToday={takenToday}
        totalToday={6}
        supplements={supplements}
      />

      {/* Main Content */}
      <div className="flex-1 overflow-hidden">
        <Tabs value={activeTab} onValueChange={setActiveTab} className="h-full flex flex-col">
          <TabsList className="grid w-full grid-cols-4 mx-4 mt-2">
            <TabsTrigger value="today" className="text-xs">Today</TabsTrigger>
            <TabsTrigger value="library" className="text-xs">Library</TabsTrigger>
            <TabsTrigger value="analytics" className="text-xs">Analytics</TabsTrigger>
            <TabsTrigger value="recommendations" className="text-xs">Insights</TabsTrigger>
          </TabsList>

          <div className="flex-1 overflow-hidden">
            <TabsContent value="today" className="h-full m-0">
              <TodaySchedule 
                supplements={supplements}
                schedules={schedules}
                intakes={todayIntakes}
                currentTime={currentTime}
              />
            </TabsContent>

            <TabsContent value="library" className="h-full m-0">
              <SupplementLibrary 
                supplements={supplements}
                schedules={schedules}
              />
            </TabsContent>

            <TabsContent value="analytics" className="h-full m-0">
              <SupplementAnalytics 
                supplements={supplements}
                intakes={todayIntakes}
                schedules={schedules}
              />
            </TabsContent>

            <TabsContent value="recommendations" className="h-full m-0">
              <SmartRecommendations 
                supplements={supplements}
                adherencePercentage={adherencePercentage}
              />
            </TabsContent>
          </div>
        </Tabs>
      </div>
    </div>
  );
}