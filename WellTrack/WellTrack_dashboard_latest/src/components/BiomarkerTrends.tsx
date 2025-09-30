import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, ResponsiveContainer, ReferenceLine, Area, AreaChart } from "recharts";
import { 
  TrendingUp,
  TrendingDown,
  Calendar,
  BarChart3,
  Target,
  AlertTriangle,
  Eye
} from "lucide-react";
import { Biomarker, BiomarkerReading } from "./Biomarkers";

interface BiomarkerTrendsProps {
  biomarkers: Biomarker[];
  readings: BiomarkerReading[];
  selectedBiomarker: Biomarker | null;
  onBiomarkerSelect: (biomarker: Biomarker) => void;
}

export function BiomarkerTrends({ 
  biomarkers, 
  readings, 
  selectedBiomarker, 
  onBiomarkerSelect 
}: BiomarkerTrendsProps) {
  const [timeRange, setTimeRange] = useState('6months');
  const [viewMode, setViewMode] = useState<'individual' | 'heatmap'>('individual');

  // Generate mock historical data for demonstration
  const generateHistoricalData = (biomarker: Biomarker) => {
    const data = [];
    const months = timeRange === '1year' ? 12 : timeRange === '6months' ? 6 : 3;
    
    for (let i = months; i >= 0; i--) {
      const date = new Date();
      date.setMonth(date.getMonth() - i);
      
      // Generate realistic variations around the current value
      const baseValue = biomarker.currentValue || biomarker.optimalRange.min + (biomarker.optimalRange.max - biomarker.optimalRange.min) / 2;
      const variation = 0.1; // 10% variation
      const value = baseValue + (Math.random() - 0.5) * baseValue * variation;
      
      data.push({
        date: date.toISOString().split('T')[0],
        value: Math.max(0, value),
        optimal_min: biomarker.optimalRange.min,
        optimal_max: biomarker.optimalRange.max,
        normal_min: biomarker.normalRange.min,
        normal_max: biomarker.normalRange.max
      });
    }
    
    return data;
  };

  const getSelectedBiomarkerData = () => {
    if (!selectedBiomarker) return [];
    return generateHistoricalData(selectedBiomarker);
  };

  const calculateTrend = (data: any[]) => {
    if (data.length < 2) return { direction: 'stable', percentage: 0 };
    
    const first = data[0].value;
    const last = data[data.length - 1].value;
    const percentage = ((last - first) / first) * 100;
    
    return {
      direction: percentage > 5 ? 'improving' : percentage < -5 ? 'declining' : 'stable',
      percentage: Math.abs(percentage)
    };
  };

  const getBiomarkerColor = (biomarker: Biomarker) => {
    switch (biomarker.category) {
      case 'hormonal': return '#f97316'; // orange
      case 'micronutrients': return '#22c55e'; // green
      case 'general_health': return '#ef4444'; // red
      default: return '#6b7280'; // gray
    }
  };

  // Heat map data for multi-biomarker view
  const getHeatMapData = () => {
    const months = timeRange === '1year' ? 12 : timeRange === '6months' ? 6 : 3;
    const data = [];
    
    for (let i = months; i >= 0; i--) {
      const date = new Date();
      date.setMonth(date.getMonth() - i);
      const monthData: any = {
        month: date.toLocaleDateString('en-US', { month: 'short', year: '2-digit' })
      };
      
      biomarkers.forEach(biomarker => {
        const baseValue = biomarker.currentValue || biomarker.optimalRange.min + (biomarker.optimalRange.max - biomarker.optimalRange.min) / 2;
        const variation = 0.1;
        const value = baseValue + (Math.random() - 0.5) * baseValue * variation;
        
        // Calculate status score (0-100)
        let score = 50;
        if (value >= biomarker.optimalRange.min && value <= biomarker.optimalRange.max) {
          score = 90;
        } else if (value >= biomarker.normalRange.min && value <= biomarker.normalRange.max) {
          score = 70;
        } else if (value <= biomarker.concerningThresholds.low || value >= biomarker.concerningThresholds.high) {
          score = 20;
        }
        
        monthData[biomarker.name] = score;
      });
      
      data.push(monthData);
    }
    
    return data;
  };

  const chartData = getSelectedBiomarkerData();
  const trend = calculateTrend(chartData);
  const heatMapData = getHeatMapData();

  return (
    <div className="h-full overflow-y-auto p-4 space-y-4">
      {/* Controls */}
      <Card>
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-base">Biomarker Trends</CardTitle>
            <div className="flex items-center gap-2">
              <Select value={viewMode} onValueChange={(value: 'individual' | 'heatmap') => setViewMode(value)}>
                <SelectTrigger className="w-32">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="individual">Individual</SelectItem>
                  <SelectItem value="heatmap">Heat Map</SelectItem>
                </SelectContent>
              </Select>
              
              <Select value={timeRange} onValueChange={setTimeRange}>
                <SelectTrigger className="w-32">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="3months">3 Months</SelectItem>
                  <SelectItem value="6months">6 Months</SelectItem>
                  <SelectItem value="1year">1 Year</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardHeader>
      </Card>

      {viewMode === 'individual' ? (
        <>
          {/* Biomarker Selection */}
          {!selectedBiomarker && (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Select a Biomarker</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 gap-2">
                  {biomarkers.map((biomarker) => (
                    <Button
                      key={biomarker.id}
                      variant="outline"
                      className="justify-start h-auto p-3"
                      onClick={() => onBiomarkerSelect(biomarker)}
                    >
                      <div className="flex items-center gap-3 w-full">
                        <div 
                          className="w-3 h-3 rounded-full"
                          style={{ backgroundColor: getBiomarkerColor(biomarker) }}
                        />
                        <div className="text-left">
                          <p className="font-medium">{biomarker.name}</p>
                          <p className="text-xs text-muted-foreground">
                            {biomarker.currentValue} {biomarker.unit} • {biomarker.trend}
                          </p>
                        </div>
                      </div>
                    </Button>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}

          {/* Individual Biomarker Chart */}
          {selectedBiomarker && chartData.length > 0 && (
            <>
              {/* Biomarker Info */}
              <Card>
                <CardContent className="p-4">
                  <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center gap-3">
                      <div 
                        className="w-4 h-4 rounded-full"
                        style={{ backgroundColor: getBiomarkerColor(selectedBiomarker) }}
                      />
                      <div>
                        <h3 className="font-medium">{selectedBiomarker.name}</h3>
                        <p className="text-sm text-muted-foreground">
                          Current: {selectedBiomarker.currentValue} {selectedBiomarker.unit}
                        </p>
                      </div>
                    </div>
                    
                    <Button 
                      variant="outline" 
                      size="sm"
                      onClick={() => onBiomarkerSelect(null as any)}
                    >
                      <Eye className="w-4 h-4 mr-1" />
                      Change
                    </Button>
                  </div>

                  {/* Trend Summary */}
                  <div className="grid grid-cols-3 gap-4 mb-4">
                    <div className="text-center">
                      <div className="flex items-center justify-center gap-1 mb-1">
                        {trend.direction === 'improving' ? (
                          <TrendingUp className="w-4 h-4 text-green-500" />
                        ) : trend.direction === 'declining' ? (
                          <TrendingDown className="w-4 h-4 text-red-500" />
                        ) : (
                          <BarChart3 className="w-4 h-4 text-blue-500" />
                        )}
                        <span className="font-medium">{trend.percentage.toFixed(1)}%</span>
                      </div>
                      <p className="text-xs text-muted-foreground capitalize">{trend.direction}</p>
                    </div>
                    
                    <div className="text-center">
                      <div className="font-medium text-green-600">
                        {selectedBiomarker.optimalRange.min}-{selectedBiomarker.optimalRange.max}
                      </div>
                      <p className="text-xs text-muted-foreground">Optimal Range</p>
                    </div>
                    
                    <div className="text-center">
                      <div className="font-medium">
                        {selectedBiomarker.lastTestDate ? 
                          new Date(selectedBiomarker.lastTestDate).toLocaleDateString() : 
                          'No data'
                        }
                      </div>
                      <p className="text-xs text-muted-foreground">Last Test</p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Chart */}
              <Card>
                <CardContent className="p-4">
                  <div className="h-64">
                    <ResponsiveContainer width="100%" height="100%">
                      <AreaChart data={chartData}>
                        <CartesianGrid strokeDasharray="3 3" stroke="#e0e7ff" />
                        <XAxis 
                          dataKey="date" 
                          tickFormatter={(value) => new Date(value).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
                          tick={{ fontSize: 12 }}
                        />
                        <YAxis 
                          domain={['dataMin - 5', 'dataMax + 5']}
                          tick={{ fontSize: 12 }}
                        />
                        
                        {/* Optimal range area */}
                        <Area
                          dataKey="optimal_max"
                          stackId="1"
                          stroke="none"
                          fill="rgba(34, 197, 94, 0.1)"
                        />
                        <Area
                          dataKey="optimal_min"
                          stackId="1"
                          stroke="none"
                          fill="white"
                        />
                        
                        {/* Reference lines for optimal range */}
                        <ReferenceLine 
                          y={selectedBiomarker.optimalRange.min} 
                          stroke="#22c55e" 
                          strokeDasharray="5 5"
                          label={{ value: "Optimal Min", position: "right" }}
                        />
                        <ReferenceLine 
                          y={selectedBiomarker.optimalRange.max} 
                          stroke="#22c55e" 
                          strokeDasharray="5 5"
                          label={{ value: "Optimal Max", position: "right" }}
                        />
                        
                        {/* Actual values line */}
                        <Line
                          type="monotone"
                          dataKey="value"
                          stroke={getBiomarkerColor(selectedBiomarker)}
                          strokeWidth={3}
                          dot={{ fill: getBiomarkerColor(selectedBiomarker), strokeWidth: 2, r: 4 }}
                          activeDot={{ r: 6, stroke: getBiomarkerColor(selectedBiomarker), strokeWidth: 2 }}
                        />
                      </AreaChart>
                    </ResponsiveContainer>
                  </div>
                </CardContent>
              </Card>

              {/* Insights for Selected Biomarker */}
              <Card>
                <CardHeader>
                  <CardTitle className="text-base flex items-center gap-2">
                    <Target className="w-4 h-4" />
                    Insights & Correlations
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div className="bg-blue-50 dark:bg-blue-950 border border-blue-200 dark:border-blue-800 rounded-lg p-3">
                    <p className="text-sm">
                      <span className="font-medium">Trend Analysis:</span> Your {selectedBiomarker.name} has been {trend.direction} by {trend.percentage.toFixed(1)}% over the selected period.
                    </p>
                  </div>
                  
                  {selectedBiomarker.name === 'Vitamin D3' && (
                    <div className="bg-green-50 dark:bg-green-950 border border-green-200 dark:border-green-800 rounded-lg p-3">
                      <p className="text-sm">
                        <span className="font-medium">Supplement Impact:</span> Your vitamin D levels show improvement correlating with increased supplementation during winter months.
                      </p>
                    </div>
                  )}
                  
                  {selectedBiomarker.name === 'Testosterone (Total)' && (
                    <div className="bg-purple-50 dark:bg-purple-950 border border-purple-200 dark:border-purple-800 rounded-lg p-3">
                      <p className="text-sm">
                        <span className="font-medium">Lifestyle Correlation:</span> Higher testosterone levels correlate with consistent strength training and adequate sleep (7+ hours).
                      </p>
                    </div>
                  )}
                </CardContent>
              </Card>
            </>
          )}
        </>
      ) : (
        /* Heat Map View */
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Biomarker Heat Map</CardTitle>
            <p className="text-sm text-muted-foreground">
              Visual overview of all biomarkers over time (green = optimal, red = concerning)
            </p>
          </CardHeader>
          <CardContent>
            <div className="overflow-x-auto">
              <div className="min-w-[600px]">
                {/* Header */}
                <div className="grid grid-cols-[120px_1fr] gap-2 mb-2">
                  <div></div>
                  <div className="grid grid-cols-7 gap-1">
                    {heatMapData.map((data, index) => (
                      <div key={index} className="text-xs text-center font-medium p-1">
                        {data.month}
                      </div>
                    ))}
                  </div>
                </div>
                
                {/* Biomarker rows */}
                {biomarkers.map((biomarker) => (
                  <div key={biomarker.id} className="grid grid-cols-[120px_1fr] gap-2 mb-1">
                    <div className="text-sm font-medium p-2 truncate">
                      {biomarker.name}
                    </div>
                    <div className="grid grid-cols-7 gap-1">
                      {heatMapData.map((data, index) => {
                        const score = data[biomarker.name] || 50;
                        const intensity = score / 100;
                        const color = score >= 80 ? 'green' : score >= 60 ? 'yellow' : 'red';
                        
                        return (
                          <div
                            key={index}
                            className={`h-8 rounded border flex items-center justify-center text-xs font-medium ${
                              color === 'green' 
                                ? 'bg-green-100 text-green-800 border-green-200 dark:bg-green-950 dark:text-green-200 dark:border-green-800'
                                : color === 'yellow'
                                ? 'bg-yellow-100 text-yellow-800 border-yellow-200 dark:bg-yellow-950 dark:text-yellow-200 dark:border-yellow-800'
                                : 'bg-red-100 text-red-800 border-red-200 dark:bg-red-950 dark:text-red-200 dark:border-red-800'
                            }`}
                            style={{ opacity: 0.3 + intensity * 0.7 }}
                          >
                            {score}
                          </div>
                        );
                      })}
                    </div>
                  </div>
                ))}
              </div>
            </div>
            
            {/* Legend */}
            <div className="flex items-center justify-center gap-6 mt-4 text-sm">
              <div className="flex items-center gap-2">
                <div className="w-4 h-4 bg-green-100 border border-green-200 rounded"></div>
                <span>Optimal (80-100)</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-4 h-4 bg-yellow-100 border border-yellow-200 rounded"></div>
                <span>Normal (60-79)</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-4 h-4 bg-red-100 border border-red-200 rounded"></div>
                <span>Concerning (0-59)</span>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}