//
// MIT License
//
// Copyright (c) 2016-present, Critical Blue Ltd.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
// (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
// publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
// subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
// ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
// THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

import SwiftUI
import shared

import UIKit

//*** UNCOMMENT THE LINE BELOW TO USE APPROOV
//import approov_service_nsurlsession

class ViewController: UIViewController {
    @IBOutlet weak var statusImageView: UIImageView!
    @IBOutlet weak var statusTextView: UILabel!

    override func viewDidLoad() {
        super.viewDidLoad()

        //*** UNCOMMENT THE LINES BELOW TO USE APPROOV
        /*ApproovService.initialize("<enter-your-config-string-here>", error: nil)
        let delegate = IOSHttpClient.companion.getDelegate()
        let session = ApproovNSURLSession.init(configuration: .default, delegate: delegate, delegateQueue: OperationQueue.current)
        IOSHttpClient.companion.setSession(pNSURLSession: session!)*/
        
        //*** UNCOMMENT THE LINE BELOW FOR APPROOV USING SECRETS PROTECTION
        //ApproovService.addSubstitutionHeader("Api-Key", requiredPrefix: nil)
    }

    @IBAction func checkHello() {
        self.statusTextView.text = "Checking connectivity..."
        self.statusImageView.image = UIImage(named: "approov")
        ApplicationApi().hello { (text) in
            if (text == "success") {
                self.statusTextView.text = "OK"
                self.statusImageView.image = UIImage(named: "hello")
                NSLog("Hello call success")
            } else {
                self.statusTextView.text = "Failure"
                self.statusImageView.image = UIImage(named: "confused")
                NSLog("Hello call failure")
            }
        }
    }
    
    @IBAction func checkShape() {
        self.statusTextView.text = "Getting a shape..."
        self.statusImageView.image = UIImage(named: "approov")
        ApplicationApi().shapes { (text) in
            switch text.lowercased() {
            case "circle":
                self.statusTextView.text = "Shapes call successful (circle)"
                self.statusImageView.image = UIImage(named: "Circle")
            case "rectangle":
                self.statusTextView.text = "Shapes call successful (rectangle)"
                self.statusImageView.image = UIImage(named: "Rectangle")
            case "square":
                self.statusTextView.text = "Shapes call successful (square)"
                self.statusImageView.image = UIImage(named: "Square")
            case "triangle":
                self.statusTextView.text = "Shapes call successful (triangle)"
                self.statusImageView.image = UIImage(named: "Triangle")
            default:
                self.statusTextView.text = "Shapes call failed \(text)"
                self.statusImageView.image = UIImage(named: "confused")
            }
            NSLog(self.statusTextView.text!)
        }
    }
}
