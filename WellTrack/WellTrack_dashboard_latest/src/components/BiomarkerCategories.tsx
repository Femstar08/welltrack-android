import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Progress } from "./ui/progress";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "./ui/collapsible";
import { 
  ChevronDown,
  ChevronUp,
  TrendingUp,
  TrendingDown,
  Minus,
  AlertTriangle,
  Activity,
  Zap,
  Heart,
  TestTube
} from "lucide-react";
import { Biomarker } from "./Biomarkers";

interface BiomarkerCategoriesProps {
  biomarkers: Biomarker[];
  selectedCategory: string | null;
  onCategorySelect: (category: string | null) => void;
  onBiomarkerSelect: (biomarker: Biomarker) => void;
}

export function BiomarkerCategories({ 
  biomarkers, 
  selectedCategory, 
  onCategorySelect, 
  onBiomarkerSelect 
}: BiomarkerCategoriesProps) {
  const [expandedCategories, setExpandedCategories] = useState<string[]>(['hormonal']);

  const categories = [
    {
      id: 'hormonal',
      name: 'Hormonal Markers',
      description: 'Testosterone, cortisol, thyroid function',
      icon: <Zap className="w-5 h-5 text-orange-500" />,
      color: 'orange'
    },
    {
      id: 'micronutrients',
      name: 'Micronutrients',
      description: 'Vitamins, minerals, and essential nutrients',
      icon: <Activity className="w-5 h-5 text-green-500" />,
      color: 'green'
    },
    {
      id: 'general_health',
      name: 'General Health Panel',
      description: 'Lipids, blood sugar, liver and kidney function',
      icon: <Heart className="w-5 h-5 text-red-500" />,
      color: 'red'
    }
  ];

  const getBiomarkersByCategory = (category: string) => {
    return biomarkers.filter(b => b.category === category);
  };

  const getBiomarkerStatus = (biomarker: Biomarker) => {
    if (!biomarker.currentValue) return 'unknown';
    
    const value = biomarker.currentValue;
    const { optimalRange, normalRange, concerningThresholds } = biomarker;
    
    if (value >= optimalRange.min && value <= optimalRange.max) return 'optimal';
    if (value >= normalRange.min && value <= normalRange.max) return 'normal';
    if (value <= concerningThresholds.low || value >= concerningThresholds.high) return 'concerning';
    return 'suboptimal';
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'optimal': return 'bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-200';
      case 'normal': return 'bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-200';
      case 'suboptimal': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-950 dark:text-yellow-200';
      case 'concerning': return 'bg-red-100 text-red-800 dark:bg-red-950 dark:text-red-200';
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-950 dark:text-gray-200';
    }
  };

  const getTrendIcon = (trend: string) => {
    switch (trend) {
      case 'improving': return <TrendingUp className="w-4 h-4 text-green-500" />;
      case 'declining': return <TrendingDown className="w-4 h-4 text-red-500" />;
      case 'stable': return <Minus className="w-4 h-4 text-blue-500" />;
      default: return <AlertTriangle className="w-4 h-4 text-gray-500" />;
    }
  };

  const getCategoryProgress = (category: string) => {
    const categoryBiomarkers = getBiomarkersByCategory(category);
    const optimalCount = categoryBiomarkers.filter(b => getBiomarkerStatus(b) === 'optimal').length;
    return (optimalCount / categoryBiomarkers.length) * 100;
  };

  const toggleCategory = (categoryId: string) => {
    if (expandedCategories.includes(categoryId)) {
      setExpandedCategories(expandedCategories.filter(id => id !== categoryId));
    } else {
      setExpandedCategories([...expandedCategories, categoryId]);
    }
  };

  return (
    <div className="h-full overflow-y-auto p-4 space-y-4">
      {categories.map((category) => {
        const categoryBiomarkers = getBiomarkersByCategory(category.id);
        const progress = getCategoryProgress(category.id);
        const isExpanded = expandedCategories.includes(category.id);
        
        return (
          <Card key={category.id} className="border border-border">
            <Collapsible open={isExpanded} onOpenChange={() => toggleCategory(category.id)}>
              <CollapsibleTrigger className="w-full">
                <CardHeader className="pb-3">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      {category.icon}
                      <div className="text-left">
                        <CardTitle className="text-base">{category.name}</CardTitle>
                        <p className="text-sm text-muted-foreground">{category.description}</p>
                      </div>
                    </div>
                    
                    <div className="flex items-center gap-3">
                      <div className="text-right">
                        <p className="text-sm font-medium">{Math.round(progress)}% Optimal</p>
                        <p className="text-xs text-muted-foreground">{categoryBiomarkers.length} markers</p>
                      </div>
                      {isExpanded ? (
                        <ChevronUp className="w-5 h-5 text-muted-foreground" />
                      ) : (
                        <ChevronDown className="w-5 h-5 text-muted-foreground" />
                      )}
                    </div>
                  </div>
                  
                  <Progress value={progress} className="h-2 mt-2" />
                </CardHeader>
              </CollapsibleTrigger>
              
              <CollapsibleContent>
                <CardContent className="pt-0 space-y-3">
                  {categoryBiomarkers.map((biomarker) => {
                    const status = getBiomarkerStatus(biomarker);
                    
                    return (
                      <Card 
                        key={biomarker.id} 
                        className="border border-border/50 cursor-pointer hover:bg-accent/50 transition-colors"
                        onClick={() => onBiomarkerSelect(biomarker)}
                      >
                        <CardContent className="p-4">
                          <div className="flex items-center justify-between mb-2">
                            <div className="flex items-center gap-2">
                              <h3 className="font-medium text-card-foreground">{biomarker.name}</h3>
                              <Badge className={`text-xs px-2 py-1 ${getStatusColor(status)}`}>
                                {status}
                              </Badge>
                              {biomarker.priority === 'high' && (
                                <AlertTriangle className="w-4 h-4 text-orange-500" />
                              )}
                            </div>
                            
                            <div className="flex items-center gap-2">
                              {getTrendIcon(biomarker.trend)}
                              <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
                                <TestTube className="w-4 h-4" />
                              </Button>
                            </div>
                          </div>

                          <div className="space-y-2">
                            {biomarker.currentValue && (
                              <div className="flex items-center justify-between text-sm">
                                <span className="text-muted-foreground">Current Value:</span>
                                <span className="font-medium">
                                  {biomarker.currentValue} {biomarker.unit}
                                </span>
                              </div>
                            )}
                            
                            <div className="flex items-center justify-between text-sm">
                              <span className="text-muted-foreground">Optimal Range:</span>
                              <span className="text-green-600">
                                {biomarker.optimalRange.min} - {biomarker.optimalRange.max} {biomarker.unit}
                              </span>
                            </div>
                            
                            {biomarker.lastTestDate && (
                              <div className="flex items-center justify-between text-sm">
                                <span className="text-muted-foreground">Last Test:</span>
                                <span>{new Date(biomarker.lastTestDate).toLocaleDateString()}</span>
                              </div>
                            )}

                            {/* Visual Range Indicator */}
                            {biomarker.currentValue && (
                              <div className="mt-3">
                                <div className="relative h-3 bg-gradient-to-r from-red-200 via-yellow-200 via-green-200 via-green-200 via-yellow-200 to-red-200 rounded-full overflow-hidden">
                                  {/* Optimal range highlighting */}
                                  <div 
                                    className="absolute top-0 h-full bg-green-400 opacity-50"
                                    style={{
                                      left: `${((biomarker.optimalRange.min - biomarker.normalRange.min) / (biomarker.normalRange.max - biomarker.normalRange.min)) * 100}%`,
                                      width: `${((biomarker.optimalRange.max - biomarker.optimalRange.min) / (biomarker.normalRange.max - biomarker.normalRange.min)) * 100}%`
                                    }}
                                  />
                                  
                                  {/* Current value indicator */}
                                  <div 
                                    className="absolute top-0 w-1 h-full bg-blue-600 shadow-lg"
                                    style={{
                                      left: `${Math.max(0, Math.min(100, ((biomarker.currentValue - biomarker.normalRange.min) / (biomarker.normalRange.max - biomarker.normalRange.min)) * 100))}%`
                                    }}
                                  />
                                </div>
                                
                                <div className="flex justify-between text-xs text-muted-foreground mt-1">
                                  <span>{biomarker.normalRange.min}</span>
                                  <span>Optimal: {biomarker.optimalRange.min}-{biomarker.optimalRange.max}</span>
                                  <span>{biomarker.normalRange.max}</span>
                                </div>
                              </div>
                            )}
                          </div>
                        </CardContent>
                      </Card>
                    );
                  })}
                </CardContent>
              </CollapsibleContent>
            </Collapsible>
          </Card>
        );
      })}

      {/* Summary Stats */}
      <Card className="bg-muted/30">
        <CardContent className="p-4">
          <h3 className="font-medium mb-3">Overall Summary</h3>
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <p className="text-muted-foreground">Total Biomarkers</p>
              <p className="font-semibold text-lg">{biomarkers.length}</p>
            </div>
            <div>
              <p className="text-muted-foreground">In Optimal Range</p>
              <p className="font-semibold text-lg text-green-600">
                {biomarkers.filter(b => getBiomarkerStatus(b) === 'optimal').length}
              </p>
            </div>
            <div>
              <p className="text-muted-foreground">Need Attention</p>
              <p className="font-semibold text-lg text-orange-600">
                {biomarkers.filter(b => getBiomarkerStatus(b) === 'concerning').length}
              </p>
            </div>
            <div>
              <p className="text-muted-foreground">High Priority</p>
              <p className="font-semibold text-lg text-red-600">
                {biomarkers.filter(b => b.priority === 'high').length}
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}