import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "./ui/collapsible";
import { 
  Brain,
  Lightbulb,
  TrendingUp,
  Eye,
  ChevronDown,
  ChevronUp,
  Target,
  Clock,
  Apple,
  Zap
} from "lucide-react";
import { useState } from "react";
import { HealthMetrics, NutritionData, TimePeriod } from "./HealthAnalytics";

interface AIInsightsPanelProps {
  metrics: HealthMetrics;
  nutritionData: NutritionData[];
  timePeriod: TimePeriod;
}

interface Insight {
  id: string;
  type: 'recommendation' | 'pattern' | 'prediction' | 'alert';
  title: string;
  description: string;
  confidence: number;
  priority: 'high' | 'medium' | 'low';
  category: string;
  actionable: boolean;
  dataPoints?: string[];
}

export function AIInsightsPanel({ metrics, nutritionData, timePeriod }: AIInsightsPanelProps) {
  const [recommendationsOpen, setRecommendationsOpen] = useState(true);
  const [patternsOpen, setPatternsOpen] = useState(true);
  const [predictionsOpen, setPredictionsOpen] = useState(false);

  // Generate AI insights based on data
  const insights: Insight[] = [
    {
      id: '1',
      type: 'recommendation',
      title: 'Optimize Protein Timing',
      description: 'Your protein intake is 15% higher on workout days. Consider distributing protein more evenly throughout the week for better muscle recovery.',
      confidence: 87,
      priority: 'high',
      category: 'Nutrition',
      actionable: true,
      dataPoints: ['Protein intake variance: 23g', 'Workout correlation: 0.82']
    },
    {
      id: '2',
      type: 'pattern',
      title: 'Weekend Nutrition Dip',
      description: 'Your nutrition scores drop by an average of 12 points on weekends. This pattern has been consistent for 3 weeks.',
      confidence: 93,
      priority: 'medium',
      category: 'Behavior',
      actionable: true,
      dataPoints: ['Weekend avg: 70', 'Weekday avg: 82', 'Consistency: 3 weeks']
    },
    {
      id: '3',
      type: 'recommendation',
      title: 'Increase Fiber Intake',
      description: 'Your fiber intake is 8g below optimal on 60% of days. Adding high-fiber snacks could improve digestive health and satiety.',
      confidence: 78,
      priority: 'medium',
      category: 'Nutrition',
      actionable: true,
      dataPoints: ['Current avg: 22g', 'Target: 30g', 'Deficit days: 60%']
    },
    {
      id: '4',
      type: 'pattern',
      title: 'Strong Supplement Adherence',
      description: 'Your supplement consistency has improved by 23% over the past month, contributing to overall wellness score gains.',
      confidence: 91,
      priority: 'low',
      category: 'Supplements',
      actionable: false,
      dataPoints: ['Improvement: +23%', 'Current rate: 88%', 'Impact: +5 wellness points']
    },
    {
      id: '5',
      type: 'prediction',
      title: 'Goal Achievement Likely',
      description: 'Based on current trends, you have an 85% chance of reaching your monthly protein goal by month-end.',
      confidence: 85,
      priority: 'low',
      category: 'Goals',
      actionable: false,
      dataPoints: ['Current pace: 102%', 'Days remaining: 12', 'Probability: 85%']
    },
    {
      id: '6',
      type: 'alert',
      title: 'Hydration Tracking Missing',
      description: 'No hydration data recorded for 5+ days. Proper hydration tracking could reveal important correlations with your energy levels.',
      confidence: 95,
      priority: 'high',
      category: 'Data Quality',
      actionable: true,
      dataPoints: ['Missing days: 5', 'Last entry: Nov 13', 'Impact: Unknown correlations']
    }
  ];

  const getInsightIcon = (type: string) => {
    switch (type) {
      case 'recommendation': return <Lightbulb className="w-4 h-4 text-yellow-500" />;
      case 'pattern': return <Eye className="w-4 h-4 text-blue-500" />;
      case 'prediction': return <TrendingUp className="w-4 h-4 text-green-500" />;
      case 'alert': return <Target className="w-4 h-4 text-red-500" />;
      default: return <Brain className="w-4 h-4 text-purple-500" />;
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'high': return 'bg-red-100 text-red-800 dark:bg-red-950 dark:text-red-200';
      case 'medium': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-950 dark:text-yellow-200';
      case 'low': return 'bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-200';
      default: return 'bg-muted text-muted-foreground';
    }
  };

  const recommendations = insights.filter(insight => insight.type === 'recommendation');
  const patterns = insights.filter(insight => insight.type === 'pattern' || insight.type === 'alert');
  const predictions = insights.filter(insight => insight.type === 'prediction');

  const InsightCard = ({ insight }: { insight: Insight }) => (
    <Card className="hover:bg-accent/50 transition-colors">
      <CardContent className="p-4">
        <div className="space-y-3">
          {/* Header */}
          <div className="flex items-start justify-between">
            <div className="flex items-start gap-3">
              {getInsightIcon(insight.type)}
              <div className="flex-1">
                <h4 className="font-medium text-card-foreground">{insight.title}</h4>
                <p className="text-sm text-muted-foreground mt-1">{insight.description}</p>
              </div>
            </div>
            <div className="flex flex-col items-end gap-1">
              <Badge className={`text-xs px-1.5 py-0.5 ${getPriorityColor(insight.priority)}`}>
                {insight.priority}
              </Badge>
              <span className="text-xs text-muted-foreground">{insight.confidence}% confident</span>
            </div>
          </div>

          {/* Data Points */}
          {insight.dataPoints && (
            <div className="bg-muted/30 rounded-lg p-2">
              <div className="space-y-1">
                {insight.dataPoints.map((point, index) => (
                  <p key={index} className="text-xs text-muted-foreground">
                    • {point}
                  </p>
                ))}
              </div>
            </div>
          )}

          {/* Action Button */}
          {insight.actionable && (
            <Button variant="outline" size="sm" className="w-full">
              Take Action
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  );

  return (
    <div className="space-y-4">
      {/* AI Insights Header */}
      <Card className="bg-purple-50 dark:bg-purple-950 border-purple-200 dark:border-purple-800">
        <CardHeader className="pb-3">
          <CardTitle className="flex items-center gap-2 text-purple-800 dark:text-purple-200">
            <Brain className="w-5 h-5" />
            AI-Powered Insights
            <Badge className="bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200 ml-auto">
              {insights.length} insights
            </Badge>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-purple-700 dark:text-purple-300">
            Based on your {timePeriod === '7d' ? 'weekly' : timePeriod === '30d' ? 'monthly' : 'long-term'} data patterns, 
            here are personalized recommendations and observations.
          </p>
        </CardContent>
      </Card>

      {/* Personalized Recommendations */}
      <Collapsible open={recommendationsOpen} onOpenChange={setRecommendationsOpen}>
        <CollapsibleTrigger className="w-full">
          <Card className="bg-yellow-50 dark:bg-yellow-950 border-yellow-200 dark:border-yellow-800 hover:bg-yellow-100 dark:hover:bg-yellow-900 transition-colors">
            <CardHeader className="pb-3">
              <CardTitle className="flex items-center justify-between text-yellow-800 dark:text-yellow-200">
                <div className="flex items-center gap-2">
                  <Lightbulb className="w-4 h-4" />
                  Personalized Recommendations
                </div>
                <div className="flex items-center gap-2">
                  <Badge className="bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200">
                    {recommendations.length}
                  </Badge>
                  {recommendationsOpen ? (
                    <ChevronUp className="w-4 h-4" />
                  ) : (
                    <ChevronDown className="w-4 h-4" />
                  )}
                </div>
              </CardTitle>
            </CardHeader>
          </Card>
        </CollapsibleTrigger>
        <CollapsibleContent className="space-y-3 mt-2">
          {recommendations.map((insight) => (
            <InsightCard key={insight.id} insight={insight} />
          ))}
        </CollapsibleContent>
      </Collapsible>

      {/* Pattern Recognition */}
      <Collapsible open={patternsOpen} onOpenChange={setPatternsOpen}>
        <CollapsibleTrigger className="w-full">
          <Card className="bg-blue-50 dark:bg-blue-950 border-blue-200 dark:border-blue-800 hover:bg-blue-100 dark:hover:bg-blue-900 transition-colors">
            <CardHeader className="pb-3">
              <CardTitle className="flex items-center justify-between text-blue-800 dark:text-blue-200">
                <div className="flex items-center gap-2">
                  <Eye className="w-4 h-4" />
                  Pattern Recognition
                </div>
                <div className="flex items-center gap-2">
                  <Badge className="bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200">
                    {patterns.length}
                  </Badge>
                  {patternsOpen ? (
                    <ChevronUp className="w-4 h-4" />
                  ) : (
                    <ChevronDown className="w-4 h-4" />
                  )}
                </div>
              </CardTitle>
            </CardHeader>
          </Card>
        </CollapsibleTrigger>
        <CollapsibleContent className="space-y-3 mt-2">
          {patterns.map((insight) => (
            <InsightCard key={insight.id} insight={insight} />
          ))}
        </CollapsibleContent>
      </Collapsible>

      {/* Predictions */}
      <Collapsible open={predictionsOpen} onOpenChange={setPredictionsOpen}>
        <CollapsibleTrigger className="w-full">
          <Card className="bg-green-50 dark:bg-green-950 border-green-200 dark:border-green-800 hover:bg-green-100 dark:hover:bg-green-900 transition-colors">
            <CardHeader className="pb-3">
              <CardTitle className="flex items-center justify-between text-green-800 dark:text-green-200">
                <div className="flex items-center gap-2">
                  <TrendingUp className="w-4 h-4" />
                  Predictive Insights
                </div>
                <div className="flex items-center gap-2">
                  <Badge className="bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200">
                    {predictions.length}
                  </Badge>
                  {predictionsOpen ? (
                    <ChevronUp className="w-4 h-4" />
                  ) : (
                    <ChevronDown className="w-4 h-4" />
                  )}
                </div>
              </CardTitle>
            </CardHeader>
          </Card>
        </CollapsibleTrigger>
        <CollapsibleContent className="space-y-3 mt-2">
          {predictions.map((insight) => (
            <InsightCard key={insight.id} insight={insight} />
          ))}
        </CollapsibleContent>
      </Collapsible>

      {/* Quick Action Suggestions */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Zap className="w-4 h-4 text-orange-500" />
            Quick Actions
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <Button variant="outline" className="w-full justify-start gap-2">
            <Apple className="w-4 h-4" />
            Log today's meals for better insights
          </Button>
          <Button variant="outline" className="w-full justify-start gap-2">
            <Clock className="w-4 h-4" />
            Set meal timing reminders
          </Button>
          <Button variant="outline" className="w-full justify-start gap-2">
            <Target className="w-4 h-4" />
            Adjust protein goal based on patterns
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}