import { useState } from "react";
import { Button } from "./ui/button";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "./ui/dropdown-menu";
import { Plus, Mic, QrCode, Camera, Edit } from "lucide-react";

export function FloatingActionButton() {
  const [isOpen, setIsOpen] = useState(false);

  const quickActions = [
    { id: 'manual', icon: Edit, label: 'Add Item', color: 'text-blue-600' },
    { id: 'voice', icon: Mic, label: 'Voice Input', color: 'text-green-600' },
    { id: 'barcode', icon: QrCode, label: 'Scan Barcode', color: 'text-purple-600' },
    { id: 'photo', icon: Camera, label: 'Take Photo', color: 'text-orange-600' }
  ];

  const handleAction = (actionId: string) => {
    switch (actionId) {
      case 'manual':
        console.log('Open manual add item dialog');
        break;
      case 'voice':
        console.log('Start voice recognition');
        break;
      case 'barcode':
        console.log('Open barcode scanner');
        break;
      case 'photo':
        console.log('Open camera for photo');
        break;
    }
    setIsOpen(false);
  };

  return (
    <div className="fixed bottom-24 right-4 z-40">
      <DropdownMenu open={isOpen} onOpenChange={setIsOpen}>
        <DropdownMenuTrigger asChild>
          <Button
            size="lg"
            className="h-14 w-14 rounded-full bg-green-500 hover:bg-green-600 text-white shadow-lg hover:shadow-xl transition-all"
          >
            <Plus className={`w-6 h-6 transition-transform ${isOpen ? 'rotate-45' : ''}`} />
          </Button>
        </DropdownMenuTrigger>
        
        <DropdownMenuContent 
          align="end" 
          side="top" 
          className="mb-2 w-48"
        >
          {quickActions.map((action) => (
            <DropdownMenuItem
              key={action.id}
              onClick={() => handleAction(action.id)}
              className="gap-3 py-3"
            >
              <action.icon className={`w-5 h-5 ${action.color}`} />
              <span className="font-medium">{action.label}</span>
            </DropdownMenuItem>
          ))}
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  );
}