import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Progress } from "./ui/progress";
import { 
  Lightbulb,
  TrendingUp,
  AlertTriangle,
  Target,
  Activity,
  Apple,
  Moon,
  Dumbbell,
  Heart,
  Brain,
  Filter,
  Star
} from "lucide-react";
import { HealthInsight, Biomarker } from "./Biomarkers";

interface BiomarkerInsightsProps {
  insights: HealthInsight[];
  biomarkers: Biomarker[];
}

export function BiomarkerInsights({ insights, biomarkers }: BiomarkerInsightsProps) {
  const [selectedType, setSelectedType] = useState<string | null>(null);
  const [showAllInsights, setShowAllInsights] = useState(false);

  const insightTypes = [
    { id: 'all', name: 'All Insights', icon: <Lightbulb className="w-4 h-4" /> },
    { id: 'correlation', name: 'Correlations', icon: <Activity className="w-4 h-4" /> },
    { id: 'improvement', name: 'Improvements', icon: <TrendingUp className="w-4 h-4" /> },
    { id: 'concern', name: 'Concerns', icon: <AlertTriangle className="w-4 h-4" /> },
    { id: 'recommendation', name: 'Recommendations', icon: <Target className="w-4 h-4" /> }
  ];

  const getInsightIcon = (type: string) => {
    switch (type) {
      case 'correlation': return <Activity className="w-5 h-5 text-blue-500" />;
      case 'improvement': return <TrendingUp className="w-5 h-5 text-green-500" />;
      case 'concern': return <AlertTriangle className="w-5 h-5 text-red-500" />;
      case 'recommendation': return <Target className="w-5 h-5 text-purple-500" />;
      default: return <Lightbulb className="w-5 h-5 text-orange-500" />;
    }
  };

  const getInsightColor = (type: string) => {
    switch (type) {
      case 'correlation': return 'border-blue-200 bg-blue-50 dark:border-blue-800 dark:bg-blue-950';
      case 'improvement': return 'border-green-200 bg-green-50 dark:border-green-800 dark:bg-green-950';
      case 'concern': return 'border-red-200 bg-red-50 dark:border-red-800 dark:bg-red-950';
      case 'recommendation': return 'border-purple-200 bg-purple-50 dark:border-purple-800 dark:bg-purple-950';
      default: return 'border-orange-200 bg-orange-50 dark:border-orange-800 dark:bg-orange-950';
    }
  };

  const getConfidenceColor = (confidence: number) => {
    if (confidence >= 80) return 'text-green-600';
    if (confidence >= 60) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getLifestyleIcon = (factor: string) => {
    if (factor.toLowerCase().includes('sleep')) return <Moon className="w-4 h-4" />;
    if (factor.toLowerCase().includes('exercise') || factor.toLowerCase().includes('training')) return <Dumbbell className="w-4 h-4" />;
    if (factor.toLowerCase().includes('stress')) return <Brain className="w-4 h-4" />;
    return <Heart className="w-4 h-4" />;
  };

  const filteredInsights = selectedType && selectedType !== 'all' 
    ? insights.filter(insight => insight.type === selectedType)
    : insights;

  const displayedInsights = showAllInsights ? filteredInsights : filteredInsights.slice(0, 5);

  // Mock correlation data
  const correlationData = [
    {
      id: '1',
      title: 'Vitamin D & Mood',
      primary: 'Vitamin D3',
      secondary: 'Mood Score',
      correlation: 0.72,
      description: 'Higher vitamin D levels correlate with improved mood scores'
    },
    {
      id: '2',
      title: 'Sleep & Cortisol',
      primary: 'Sleep Quality',
      secondary: 'Cortisol (Morning)',
      correlation: -0.68,
      description: 'Better sleep quality correlates with lower morning cortisol'
    },
    {
      id: '3',
      title: 'Exercise & Testosterone',
      primary: 'Strength Training',
      secondary: 'Testosterone (Total)',
      correlation: 0.65,
      description: 'Regular strength training correlates with higher testosterone'
    }
  ];

  return (
    <div className="h-full overflow-y-auto p-4 space-y-4">
      {/* Filter Controls */}
      <Card>
        <CardContent className="p-4">
          <div className="flex items-center gap-2 flex-wrap">
            <Filter className="w-4 h-4 text-muted-foreground" />
            {insightTypes.map((type) => (
              <Button
                key={type.id}
                variant={selectedType === type.id ? "default" : "outline"}
                size="sm"
                className="gap-1"
                onClick={() => setSelectedType(selectedType === type.id ? null : type.id)}
              >
                {type.icon}
                {type.name}
              </Button>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* AI Insights Summary */}
      <Card className="bg-gradient-to-br from-blue-50 to-purple-50 dark:from-blue-950 dark:to-purple-950 border-blue-200 dark:border-blue-800">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Brain className="w-5 h-5 text-purple-500" />
            AI Health Summary
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="bg-white/50 dark:bg-black/20 rounded-lg p-3">
            <p className="text-sm">
              <span className="font-medium">Overall Assessment:</span> Your biomarker profile shows 
              <span className="text-green-600 font-medium"> 3 improving trends</span> and 
              <span className="text-blue-600 font-medium"> 4 stable markers</span>. 
              Priority focus areas include vitamin D optimization and sleep quality improvement.
            </p>
          </div>
          
          <div className="grid grid-cols-3 gap-3 text-center">
            <div>
              <div className="text-lg font-semibold text-green-600">85%</div>
              <p className="text-xs text-muted-foreground">Health Score</p>
            </div>
            <div>
              <div className="text-lg font-semibold text-blue-600">6</div>
              <p className="text-xs text-muted-foreground">Active Insights</p>
            </div>
            <div>
              <div className="text-lg font-semibold text-purple-600">High</div>
              <p className="text-xs text-muted-foreground">Confidence</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Individual Insights */}
      <div className="space-y-3">
        {displayedInsights.map((insight) => (
          <Card key={insight.id} className={getInsightColor(insight.type)}>
            <CardContent className="p-4">
              <div className="flex items-start gap-3">
                {getInsightIcon(insight.type)}
                <div className="flex-1">
                  <div className="flex items-center justify-between mb-2">
                    <h3 className="font-medium text-card-foreground">{insight.title}</h3>
                    <div className="flex items-center gap-2">
                      <Badge variant="secondary" className="text-xs">
                        {insight.type}
                      </Badge>
                      <div className={`text-xs font-medium ${getConfidenceColor(insight.confidence)}`}>
                        {insight.confidence}% confidence
                      </div>
                    </div>
                  </div>
                  
                  <p className="text-sm text-muted-foreground mb-3">
                    {insight.description}
                  </p>
                  
                  {/* Related Biomarkers */}
                  <div className="flex items-center gap-2 mb-2">
                    <span className="text-xs font-medium text-muted-foreground">Biomarkers:</span>
                    <div className="flex flex-wrap gap-1">
                      {insight.biomarkers.map((biomarkerId) => {
                        const biomarker = biomarkers.find(b => b.id === biomarkerId);
                        return biomarker ? (
                          <Badge key={biomarkerId} variant="outline" className="text-xs">
                            {biomarker.name}
                          </Badge>
                        ) : null;
                      })}
                    </div>
                  </div>
                  
                  {/* Nutrition Factors */}
                  {insight.nutritionFactors && insight.nutritionFactors.length > 0 && (
                    <div className="flex items-center gap-2 mb-2">
                      <Apple className="w-3 h-3 text-green-500" />
                      <span className="text-xs font-medium text-muted-foreground">Nutrition:</span>
                      <div className="flex flex-wrap gap-1">
                        {insight.nutritionFactors.map((factor, index) => (
                          <Badge key={index} variant="outline" className="text-xs bg-green-50 text-green-700 border-green-200 dark:bg-green-950 dark:text-green-300 dark:border-green-800">
                            {factor}
                          </Badge>
                        ))}
                      </div>
                    </div>
                  )}
                  
                  {/* Lifestyle Factors */}
                  {insight.lifestyleFactors && insight.lifestyleFactors.length > 0 && (
                    <div className="flex items-center gap-2">
                      <Activity className="w-3 h-3 text-blue-500" />
                      <span className="text-xs font-medium text-muted-foreground">Lifestyle:</span>
                      <div className="flex flex-wrap gap-1">
                        {insight.lifestyleFactors.map((factor, index) => (
                          <Badge key={index} variant="outline" className="text-xs bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-950 dark:text-blue-300 dark:border-blue-800">
                            {getLifestyleIcon(factor)}
                            {factor}
                          </Badge>
                        ))}
                      </div>
                    </div>
                  )}
                  
                  {/* Confidence Bar */}
                  <div className="mt-3">
                    <div className="flex items-center justify-between text-xs mb-1">
                      <span className="text-muted-foreground">Confidence Level</span>
                      <span className={getConfidenceColor(insight.confidence)}>
                        {insight.confidence}%
                      </span>
                    </div>
                    <Progress value={insight.confidence} className="h-1" />
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Show More Button */}
      {filteredInsights.length > 5 && !showAllInsights && (
        <Card className="border-dashed border-2">
          <CardContent className="p-4 text-center">
            <Button variant="outline" onClick={() => setShowAllInsights(true)}>
              Show {filteredInsights.length - 5} More Insights
            </Button>
          </CardContent>
        </Card>
      )}

      {/* Correlation Analysis */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Activity className="w-5 h-5 text-blue-500" />
            Correlation Analysis
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          {correlationData.map((correlation) => (
            <div key={correlation.id} className="bg-muted/30 rounded-lg p-3">
              <div className="flex items-center justify-between mb-2">
                <h4 className="font-medium">{correlation.title}</h4>
                <div className="flex items-center gap-1">
                  <Star className="w-4 h-4 text-yellow-500" />
                  <span className="text-sm font-medium">
                    {Math.abs(correlation.correlation).toFixed(2)}
                  </span>
                </div>
              </div>
              
              <p className="text-sm text-muted-foreground mb-2">
                {correlation.description}
              </p>
              
              <div className="flex items-center gap-2 text-xs">
                <Badge variant="outline">{correlation.primary}</Badge>
                <span className="text-muted-foreground">
                  {correlation.correlation > 0 ? '↗️' : '↘️'}
                </span>
                <Badge variant="outline">{correlation.secondary}</Badge>
              </div>
            </div>
          ))}
        </CardContent>
      </Card>

      {/* Action Items */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Target className="w-5 h-5 text-purple-500" />
            Recommended Actions
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="bg-purple-50 dark:bg-purple-950 border border-purple-200 dark:border-purple-800 rounded-lg p-3">
            <div className="flex items-center gap-2 mb-1">
              <Sun className="w-4 h-4 text-yellow-500" />
              <span className="font-medium text-sm">Increase Vitamin D intake</span>
            </div>
            <p className="text-xs text-muted-foreground">
              Consider increasing daily vitamin D3 supplementation to 2000-3000 IU during winter months.
            </p>
          </div>
          
          <div className="bg-blue-50 dark:bg-blue-950 border border-blue-200 dark:border-blue-800 rounded-lg p-3">
            <div className="flex items-center gap-2 mb-1">
              <Moon className="w-4 h-4 text-blue-500" />
              <span className="font-medium text-sm">Optimize sleep schedule</span>
            </div>
            <p className="text-xs text-muted-foreground">
              Maintain consistent 7-8 hour sleep schedule to support cortisol regulation.
            </p>
          </div>
          
          <div className="bg-green-50 dark:bg-green-950 border border-green-200 dark:border-green-800 rounded-lg p-3">
            <div className="flex items-center gap-2 mb-1">
              <Dumbbell className="w-4 h-4 text-green-500" />
              <span className="font-medium text-sm">Continue strength training</span>
            </div>
            <p className="text-xs text-muted-foreground">
              Your current exercise routine is supporting healthy testosterone levels. Keep it up!
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}