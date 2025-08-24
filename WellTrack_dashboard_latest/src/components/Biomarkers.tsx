import { useState } from "react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./ui/tabs";
import { BiomarkerHeader } from "./BiomarkerHeader";
import { BiomarkerCategories } from "./BiomarkerCategories";
import { BiomarkerEntry } from "./BiomarkerEntry";
import { BiomarkerTrends } from "./BiomarkerTrends";
import { BiomarkerInsights } from "./BiomarkerInsights";
import { TestScheduling } from "./TestScheduling";

export interface Biomarker {
  id: string;
  name: string;
  category: 'hormonal' | 'micronutrients' | 'general_health';
  unit: string;
  optimalRange: { min: number; max: number };
  normalRange: { min: number; max: number };
  concerningThresholds: { low: number; high: number };
  currentValue?: number;
  lastTestDate?: string;
  trend: 'improving' | 'stable' | 'declining' | 'unknown';
  priority: 'high' | 'medium' | 'low';
}

export interface BiomarkerReading {
  id: string;
  biomarkerId: string;
  value: number;
  testDate: string;
  labSource: string;
  notes?: string;
  fasting?: boolean;
}

export interface TestPanel {
  id: string;
  name: string;
  biomarkerIds: string[];
  frequency: 'monthly' | 'quarterly' | 'bi-annual' | 'annual';
  cost?: number;
  fastingRequired: boolean;
  description: string;
}

export interface HealthInsight {
  id: string;
  type: 'correlation' | 'improvement' | 'concern' | 'recommendation';
  title: string;
  description: string;
  biomarkers: string[];
  nutritionFactors?: string[];
  lifestyleFactors?: string[];
  confidence: number; // 0-100
  dateGenerated: string;
}

export function Biomarkers() {
  const [activeTab, setActiveTab] = useState("overview");
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [showEntryModal, setShowEntryModal] = useState(false);
  const [selectedBiomarker, setSelectedBiomarker] = useState<Biomarker | null>(null);

  // Mock biomarkers data
  const biomarkers: Biomarker[] = [
    // Hormonal Markers
    {
      id: '1',
      name: 'Testosterone (Total)',
      category: 'hormonal',
      unit: 'ng/dL',
      optimalRange: { min: 600, max: 1000 },
      normalRange: { min: 300, max: 1200 },
      concerningThresholds: { low: 250, high: 1500 },
      currentValue: 720,
      lastTestDate: '2024-10-15',
      trend: 'stable',
      priority: 'high'
    },
    {
      id: '2',
      name: 'Cortisol (Morning)',
      category: 'hormonal',
      unit: 'μg/dL',
      optimalRange: { min: 10, max: 20 },
      normalRange: { min: 6, max: 23 },
      concerningThresholds: { low: 3, high: 30 },
      currentValue: 16,
      lastTestDate: '2024-10-15',
      trend: 'improving',
      priority: 'medium'
    },
    {
      id: '3',
      name: 'TSH',
      category: 'hormonal',
      unit: 'mIU/L',
      optimalRange: { min: 1, max: 2 },
      normalRange: { min: 0.4, max: 4.0 },
      concerningThresholds: { low: 0.1, high: 10 },
      currentValue: 1.5,
      lastTestDate: '2024-10-15',
      trend: 'stable',
      priority: 'medium'
    },
    // Micronutrients
    {
      id: '4',
      name: 'Vitamin D3',
      category: 'micronutrients',
      unit: 'ng/mL',
      optimalRange: { min: 40, max: 80 },
      normalRange: { min: 30, max: 100 },
      concerningThresholds: { low: 20, high: 150 },
      currentValue: 32,
      lastTestDate: '2024-10-15',
      trend: 'improving',
      priority: 'high'
    },
    {
      id: '5',
      name: 'B12',
      category: 'micronutrients',
      unit: 'pg/mL',
      optimalRange: { min: 500, max: 1500 },
      normalRange: { min: 200, max: 900 },
      concerningThresholds: { low: 150, high: 2000 },
      currentValue: 650,
      lastTestDate: '2024-10-15',
      trend: 'stable',
      priority: 'medium'
    },
    {
      id: '6',
      name: 'Ferritin',
      category: 'micronutrients',
      unit: 'ng/mL',
      optimalRange: { min: 50, max: 150 },
      normalRange: { min: 12, max: 300 },
      concerningThresholds: { low: 5, high: 500 },
      currentValue: 85,
      lastTestDate: '2024-10-15',
      trend: 'stable',
      priority: 'medium'
    },
    // General Health
    {
      id: '7',
      name: 'Total Cholesterol',
      category: 'general_health',
      unit: 'mg/dL',
      optimalRange: { min: 150, max: 200 },
      normalRange: { min: 100, max: 240 },
      concerningThresholds: { low: 80, high: 300 },
      currentValue: 180,
      lastTestDate: '2024-10-15',
      trend: 'improving',
      priority: 'medium'
    },
    {
      id: '8',
      name: 'HbA1C',
      category: 'general_health',
      unit: '%',
      optimalRange: { min: 4.5, max: 5.4 },
      normalRange: { min: 4.0, max: 5.7 },
      concerningThresholds: { low: 3.5, high: 9.0 },
      currentValue: 5.1,
      lastTestDate: '2024-10-15',
      trend: 'stable',
      priority: 'high'
    }
  ];

  // Mock readings data
  const readings: BiomarkerReading[] = [
    {
      id: '1',
      biomarkerId: '1',
      value: 720,
      testDate: '2024-10-15',
      labSource: 'LabCorp',
      fasting: true
    },
    {
      id: '2',
      biomarkerId: '1',
      value: 680,
      testDate: '2024-07-15',
      labSource: 'LabCorp',
      fasting: true
    },
    {
      id: '3',
      biomarkerId: '4',
      value: 32,
      testDate: '2024-10-15',
      labSource: 'LabCorp'
    },
    {
      id: '4',
      biomarkerId: '4',
      value: 25,
      testDate: '2024-07-15',
      labSource: 'LabCorp'
    }
  ];

  // Mock test panels
  const testPanels: TestPanel[] = [
    {
      id: '1',
      name: 'Complete Hormone Panel',
      biomarkerIds: ['1', '2', '3'],
      frequency: 'quarterly',
      cost: 250,
      fastingRequired: true,
      description: 'Comprehensive hormone assessment including testosterone, cortisol, and thyroid function'
    },
    {
      id: '2',
      name: 'Micronutrient Panel',
      biomarkerIds: ['4', '5', '6'],
      frequency: 'bi-annual',
      cost: 180,
      fastingRequired: false,
      description: 'Essential vitamins and minerals assessment'
    },
    {
      id: '3',
      name: 'Metabolic Panel',
      biomarkerIds: ['7', '8'],
      frequency: 'annual',
      cost: 120,
      fastingRequired: true,
      description: 'Blood sugar and cholesterol monitoring'
    }
  ];

  // Mock insights
  const insights: HealthInsight[] = [
    {
      id: '1',
      type: 'improvement',
      title: 'Vitamin D levels improving',
      description: 'Your vitamin D3 has increased by 28% since starting supplementation 3 months ago.',
      biomarkers: ['4'],
      nutritionFactors: ['Vitamin D3 supplement'],
      confidence: 85,
      dateGenerated: '2024-10-16'
    },
    {
      id: '2',
      type: 'correlation',
      title: 'Exercise correlation with testosterone',
      description: 'Your testosterone levels show a positive correlation with strength training frequency.',
      biomarkers: ['1'],
      lifestyleFactors: ['strength training', 'sleep quality'],
      confidence: 72,
      dateGenerated: '2024-10-15'
    },
    {
      id: '3',
      type: 'recommendation',
      title: 'Consider iron intake assessment',
      description: 'Your ferritin levels are stable but on the lower end of optimal. Consider tracking iron-rich foods.',
      biomarkers: ['6'],
      nutritionFactors: ['iron intake', 'vitamin C'],
      confidence: 68,
      dateGenerated: '2024-10-14'
    }
  ];

  const getHealthScore = () => {
    const inOptimalRange = biomarkers.filter(b => 
      b.currentValue && 
      b.currentValue >= b.optimalRange.min && 
      b.currentValue <= b.optimalRange.max
    ).length;
    
    return Math.round((inOptimalRange / biomarkers.length) * 100);
  };

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <BiomarkerHeader 
        healthScore={getHealthScore()}
        lastTestDate="2024-10-15"
        nextTestDate="2025-01-15"
        onAddResults={() => setShowEntryModal(true)}
      />

      {/* Main Content */}
      <div className="flex-1 overflow-hidden">
        <Tabs value={activeTab} onValueChange={setActiveTab} className="h-full flex flex-col">
          <TabsList className="grid w-full grid-cols-5 mx-4 mt-2">
            <TabsTrigger value="overview" className="text-xs">Overview</TabsTrigger>
            <TabsTrigger value="trends" className="text-xs">Trends</TabsTrigger>
            <TabsTrigger value="insights" className="text-xs">Insights</TabsTrigger>
            <TabsTrigger value="schedule" className="text-xs">Schedule</TabsTrigger>
            <TabsTrigger value="entry" className="text-xs">Add Data</TabsTrigger>
          </TabsList>

          <div className="flex-1 overflow-hidden">
            <TabsContent value="overview" className="h-full m-0">
              <BiomarkerCategories 
                biomarkers={biomarkers}
                selectedCategory={selectedCategory}
                onCategorySelect={setSelectedCategory}
                onBiomarkerSelect={setSelectedBiomarker}
              />
            </TabsContent>

            <TabsContent value="trends" className="h-full m-0">
              <BiomarkerTrends 
                biomarkers={biomarkers}
                readings={readings}
                selectedBiomarker={selectedBiomarker}
                onBiomarkerSelect={setSelectedBiomarker}
              />
            </TabsContent>

            <TabsContent value="insights" className="h-full m-0">
              <BiomarkerInsights 
                insights={insights}
                biomarkers={biomarkers}
              />
            </TabsContent>

            <TabsContent value="schedule" className="h-full m-0">
              <TestScheduling 
                testPanels={testPanels}
                biomarkers={biomarkers}
                readings={readings}
              />
            </TabsContent>

            <TabsContent value="entry" className="h-full m-0">
              <BiomarkerEntry 
                biomarkers={biomarkers}
                testPanels={testPanels}
                onDataAdded={() => console.log('Data added')}
              />
            </TabsContent>
          </div>
        </Tabs>
      </div>
    </div>
  );
}