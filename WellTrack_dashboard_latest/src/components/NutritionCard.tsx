import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Progress } from "./ui/progress";
import { Badge } from "./ui/badge";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "./ui/collapsible";
import { Activity, ChevronDown, ChevronUp } from "lucide-react";
import { useState } from "react";

interface NutrientProgress {
  name: string;
  current: number;
  target: number;
  unit: string;
  grade: 'A' | 'B' | 'C' | 'D' | 'E';
}

export function NutritionCard() {
  const [isOpen, setIsOpen] = useState(true);
  
  const nutrients: NutrientProgress[] = [
    { name: "Calories", current: 1250, target: 2000, unit: "cal", grade: "B" },
    { name: "Protein", current: 85, target: 120, unit: "g", grade: "A" },
    { name: "Carbs", current: 180, target: 250, unit: "g", grade: "B" },
    { name: "Fats", current: 45, target: 67, unit: "g", grade: "B" },
    { name: "Fiber", current: 15, target: 25, unit: "g", grade: "C" },
    { name: "Water", current: 6, target: 8, unit: "cups", grade: "B" }
  ];

  const getGradeColor = (grade: string) => {
    switch (grade) {
      case 'A': return "bg-green-500 text-white dark:bg-green-600";
      case 'B': return "bg-green-400 text-white dark:bg-green-500";
      case 'C': return "bg-yellow-500 text-white dark:bg-yellow-600";
      case 'D': return "bg-orange-500 text-white dark:bg-orange-600";
      case 'E': return "bg-red-500 text-white dark:bg-red-600";
      default: return "bg-muted text-muted-foreground";
    }
  };

  const getProgressColor = (grade: string) => {
    switch (grade) {
      case 'A': return "bg-green-500";
      case 'B': return "bg-green-400";
      case 'C': return "bg-yellow-500";
      case 'D': return "bg-orange-500";
      case 'E': return "bg-red-500";
      default: return "bg-muted";
    }
  };

  const overallGrade = Math.round(nutrients.reduce((sum, n) => sum + ['E','D','C','B','A'].indexOf(n.grade), 0) / nutrients.length);
  const overallGradeLabel = ['E','D','C','B','A'][overallGrade];

  return (
    <Card className="bg-card shadow-sm border border-border">
      <Collapsible open={isOpen} onOpenChange={setIsOpen}>
        <CollapsibleTrigger className="w-full">
          <CardHeader className="pb-3 px-4 py-4">
            <CardTitle className="flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <Activity className="w-6 h-6 text-blue-600" />
                <span className="text-lg text-card-foreground">Nutrition Progress</span>
              </div>
              <div className="flex items-center space-x-2">
                <Badge className={`w-8 h-8 text-sm p-0 flex items-center justify-center ${getGradeColor(overallGradeLabel)}`}>
                  {overallGradeLabel}
                </Badge>
                {isOpen ? (
                  <ChevronUp className="w-5 h-5 text-muted-foreground" />
                ) : (
                  <ChevronDown className="w-5 h-5 text-muted-foreground" />
                )}
              </div>
            </CardTitle>
          </CardHeader>
        </CollapsibleTrigger>
        
        <CollapsibleContent>
          <CardContent className="px-4 pb-4 space-y-5">
            {nutrients.map((nutrient, index) => (
              <div key={index} className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-base text-card-foreground font-medium">{nutrient.name}</span>
                  <div className="flex items-center space-x-3">
                    <span className="text-sm text-muted-foreground">
                      {nutrient.current}/{nutrient.target} {nutrient.unit}
                    </span>
                    <Badge className={`w-7 h-7 text-sm p-0 flex items-center justify-center ${getGradeColor(nutrient.grade)}`}>
                      {nutrient.grade}
                    </Badge>
                  </div>
                </div>
                <div className="relative">
                  <Progress 
                    value={(nutrient.current / nutrient.target) * 100} 
                    className="h-3"
                  />
                  <div 
                    className={`absolute top-0 left-0 h-3 rounded-full ${getProgressColor(nutrient.grade)}`}
                    style={{ width: `${Math.min((nutrient.current / nutrient.target) * 100, 100)}%` }}
                  />
                </div>
              </div>
            ))}
          </CardContent>
        </CollapsibleContent>
      </Collapsible>
    </Card>
  );
}