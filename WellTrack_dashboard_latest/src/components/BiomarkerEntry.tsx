import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { Textarea } from "./ui/textarea";
import { Switch } from "./ui/switch";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./ui/tabs";
import { 
  Plus,
  Scan,
  Camera,
  Mic,
  Upload,
  Calendar,
  TestTube,
  Check,
  X,
  FileText
} from "lucide-react";
import { Biomarker, TestPanel } from "./Biomarkers";

interface BiomarkerEntryProps {
  biomarkers: Biomarker[];
  testPanels: TestPanel[];
  onDataAdded: () => void;
}

export function BiomarkerEntry({ biomarkers, testPanels, onDataAdded }: BiomarkerEntryProps) {
  const [entryMode, setEntryMode] = useState<'quick' | 'manual' | 'photo' | 'voice'>('quick');
  const [selectedPanel, setSelectedPanel] = useState<string>('');
  const [testDate, setTestDate] = useState(new Date().toISOString().split('T')[0]);
  const [labSource, setLabSource] = useState('');
  const [isFasting, setIsFasting] = useState(false);
  const [notes, setNotes] = useState('');
  const [biomarkerValues, setBiomarkerValues] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleValueChange = (biomarkerId: string, value: string) => {
    setBiomarkerValues(prev => ({ ...prev, [biomarkerId]: value }));
  };

  const handleSubmit = async () => {
    setIsSubmitting(true);
    
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    console.log('Submitting biomarker data:', {
      testDate,
      labSource,
      isFasting,
      notes,
      values: biomarkerValues
    });
    
    setIsSubmitting(false);
    onDataAdded();
  };

  const getSelectedPanelBiomarkers = () => {
    if (!selectedPanel) return [];
    const panel = testPanels.find(p => p.id === selectedPanel);
    if (!panel) return [];
    
    return biomarkers.filter(b => panel.biomarkerIds.includes(b.id));
  };

  const labSources = [
    'LabCorp',
    'Quest Diagnostics',
    'Mayo Clinic Labs',
    'ARUP Laboratories',
    'BioReference',
    'Sonic Healthcare',
    'Other'
  ];

  return (
    <div className="h-full overflow-y-auto p-4 space-y-4">
      {/* Entry Mode Selection */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Add Biomarker Results</CardTitle>
        </CardHeader>
        <CardContent>
          <Tabs value={entryMode} onValueChange={(value: any) => setEntryMode(value)}>
            <TabsList className="grid w-full grid-cols-4">
              <TabsTrigger value="quick" className="text-xs">Quick Entry</TabsTrigger>
              <TabsTrigger value="manual" className="text-xs">Manual</TabsTrigger>
              <TabsTrigger value="photo" className="text-xs">Photo/OCR</TabsTrigger>
              <TabsTrigger value="voice" className="text-xs">Voice</TabsTrigger>
            </TabsList>

            {/* Quick Entry Mode */}
            <TabsContent value="quick" className="space-y-4 mt-4">
              <div className="space-y-3">
                <Label>Select Test Panel</Label>
                <Select value={selectedPanel} onValueChange={setSelectedPanel}>
                  <SelectTrigger>
                    <SelectValue placeholder="Choose a common test panel..." />
                  </SelectTrigger>
                  <SelectContent>
                    {testPanels.map((panel) => (
                      <SelectItem key={panel.id} value={panel.id}>
                        <div className="flex flex-col items-start">
                          <span className="font-medium">{panel.name}</span>
                          <span className="text-xs text-muted-foreground">
                            {panel.biomarkerIds.length} markers • ${panel.cost}
                          </span>
                        </div>
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>

                {selectedPanel && (
                  <div className="bg-muted/30 rounded-lg p-3">
                    <h4 className="font-medium mb-2">
                      {testPanels.find(p => p.id === selectedPanel)?.name}
                    </h4>
                    <p className="text-sm text-muted-foreground mb-3">
                      {testPanels.find(p => p.id === selectedPanel)?.description}
                    </p>
                    
                    <div className="space-y-3">
                      {getSelectedPanelBiomarkers().map((biomarker) => (
                        <div key={biomarker.id} className="flex items-center gap-3">
                          <div className="flex-1">
                            <Label className="text-sm">{biomarker.name}</Label>
                            <p className="text-xs text-muted-foreground">
                              Optimal: {biomarker.optimalRange.min}-{biomarker.optimalRange.max} {biomarker.unit}
                            </p>
                          </div>
                          <div className="w-24">
                            <Input
                              placeholder="Value"
                              value={biomarkerValues[biomarker.id] || ''}
                              onChange={(e) => handleValueChange(biomarker.id, e.target.value)}
                            />
                          </div>
                          <span className="text-sm text-muted-foreground w-12">
                            {biomarker.unit}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </TabsContent>

            {/* Manual Entry Mode */}
            <TabsContent value="manual" className="space-y-4 mt-4">
              <div className="space-y-3">
                <Label>Select Individual Biomarkers</Label>
                <div className="grid grid-cols-2 gap-2 max-h-32 overflow-y-auto">
                  {biomarkers.map((biomarker) => (
                    <Button
                      key={biomarker.id}
                      variant={biomarkerValues[biomarker.id] ? "default" : "outline"}
                      size="sm"
                      className="justify-start h-auto p-2"
                      onClick={() => {
                        if (biomarkerValues[biomarker.id]) {
                          const newValues = { ...biomarkerValues };
                          delete newValues[biomarker.id];
                          setBiomarkerValues(newValues);
                        } else {
                          handleValueChange(biomarker.id, '');
                        }
                      }}
                    >
                      <div className="text-left">
                        <p className="text-xs font-medium">{biomarker.name}</p>
                        <p className="text-xs text-muted-foreground">{biomarker.unit}</p>
                      </div>
                    </Button>
                  ))}
                </div>

                {Object.keys(biomarkerValues).length > 0 && (
                  <div className="space-y-3">
                    <h4 className="font-medium">Enter Values</h4>
                    {Object.keys(biomarkerValues).map((biomarkerId) => {
                      const biomarker = biomarkers.find(b => b.id === biomarkerId);
                      if (!biomarker) return null;

                      return (
                        <div key={biomarkerId} className="flex items-center gap-3">
                          <div className="flex-1">
                            <Label className="text-sm">{biomarker.name}</Label>
                            <p className="text-xs text-muted-foreground">
                              Optimal: {biomarker.optimalRange.min}-{biomarker.optimalRange.max} {biomarker.unit}
                            </p>
                          </div>
                          <div className="w-24">
                            <Input
                              placeholder="Value"
                              value={biomarkerValues[biomarkerId]}
                              onChange={(e) => handleValueChange(biomarkerId, e.target.value)}
                            />
                          </div>
                          <span className="text-sm text-muted-foreground w-12">
                            {biomarker.unit}
                          </span>
                          <Button
                            variant="ghost"
                            size="sm"
                            className="h-8 w-8 p-0"
                            onClick={() => {
                              const newValues = { ...biomarkerValues };
                              delete newValues[biomarkerId];
                              setBiomarkerValues(newValues);
                            }}
                          >
                            <X className="w-4 h-4" />
                          </Button>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            </TabsContent>

            {/* Photo/OCR Entry Mode */}
            <TabsContent value="photo" className="space-y-4 mt-4">
              <div className="text-center space-y-4">
                <div className="border-2 border-dashed border-border rounded-lg p-8">
                  <Camera className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                  <h3 className="font-medium mb-2">Capture Lab Report</h3>
                  <p className="text-sm text-muted-foreground mb-4">
                    Take a photo of your lab results and we'll extract the values automatically using OCR.
                  </p>
                  <div className="space-y-2">
                    <Button className="w-full">
                      <Camera className="w-4 h-4 mr-2" />
                      Take Photo
                    </Button>
                    <Button variant="outline" className="w-full">
                      <Upload className="w-4 h-4 mr-2" />
                      Upload Image
                    </Button>
                  </div>
                </div>

                <div className="bg-blue-50 dark:bg-blue-950 border border-blue-200 dark:border-blue-800 rounded-lg p-3">
                  <p className="text-sm">
                    <span className="font-medium">Pro Tip:</span> For best results, ensure good lighting and 
                    that all text is clearly readable. We support most major lab formats.
                  </p>
                </div>
              </div>
            </TabsContent>

            {/* Voice Entry Mode */}
            <TabsContent value="voice" className="space-y-4 mt-4">
              <div className="text-center space-y-4">
                <div className="border-2 border-dashed border-border rounded-lg p-8">
                  <Mic className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                  <h3 className="font-medium mb-2">Voice Input</h3>
                  <p className="text-sm text-muted-foreground mb-4">
                    Speak your lab results and we'll transcribe them automatically. 
                    Just say "Testosterone 720 nanograms per deciliter" for example.
                  </p>
                  <Button size="lg" className="bg-red-500 hover:bg-red-600">
                    <Mic className="w-5 h-5 mr-2" />
                    Start Recording
                  </Button>
                </div>

                <div className="bg-orange-50 dark:bg-orange-950 border border-orange-200 dark:border-orange-800 rounded-lg p-3">
                  <p className="text-sm">
                    <span className="font-medium">Example:</span> "My vitamin D is 32 nanograms per milliliter, 
                    testosterone total is 720 nanograms per deciliter, and cortisol morning is 16 micrograms per deciliter."
                  </p>
                </div>
              </div>
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>

      {/* Test Information */}
      {(selectedPanel || Object.keys(biomarkerValues).length > 0) && (
        <Card>
          <CardHeader>
            <CardTitle className="text-base flex items-center gap-2">
              <TestTube className="w-4 h-4" />
              Test Information
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Test Date</Label>
                <Input
                  type="date"
                  value={testDate}
                  onChange={(e) => setTestDate(e.target.value)}
                />
              </div>
              
              <div className="space-y-2">
                <Label>Lab Source</Label>
                <Select value={labSource} onValueChange={setLabSource}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select lab..." />
                  </SelectTrigger>
                  <SelectContent>
                    {labSources.map((lab) => (
                      <SelectItem key={lab} value={lab}>
                        {lab}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="flex items-center space-x-2">
              <Switch
                id="fasting"
                checked={isFasting}
                onCheckedChange={setIsFasting}
              />
              <Label htmlFor="fasting">Fasting test (no food 8-12 hours prior)</Label>
            </div>

            <div className="space-y-2">
              <Label>Notes (Optional)</Label>
              <Textarea
                placeholder="Any additional context about this test (medications, illness, etc.)..."
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                rows={3}
              />
            </div>
          </CardContent>
        </Card>
      )}

      {/* Submit Button */}
      {(selectedPanel || Object.keys(biomarkerValues).length > 0) && (
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="font-medium">
                  Ready to submit {selectedPanel ? getSelectedPanelBiomarkers().length : Object.keys(biomarkerValues).length} biomarker results
                </p>
                <p className="text-sm text-muted-foreground">
                  Test date: {new Date(testDate).toLocaleDateString()}
                  {labSource && ` • ${labSource}`}
                  {isFasting && ' • Fasting'}
                </p>
              </div>
              
              <Button 
                onClick={handleSubmit}
                disabled={isSubmitting}
                className="gap-2"
              >
                {isSubmitting ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    Saving...
                  </>
                ) : (
                  <>
                    <Check className="w-4 h-4" />
                    Submit Results
                  </>
                )}
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Import Options</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <Button variant="outline" className="w-full justify-start gap-2">
            <Scan className="w-4 h-4" />
            Scan Barcode from Lab Report
          </Button>
          
          <Button variant="outline" className="w-full justify-start gap-2">
            <FileText className="w-4 h-4" />
            Import from PDF Report
          </Button>
          
          <Button variant="outline" className="w-full justify-start gap-2">
            <Upload className="w-4 h-4" />
            Sync from Health Platform
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}