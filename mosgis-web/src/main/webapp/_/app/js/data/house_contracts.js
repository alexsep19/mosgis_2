define ([], function () {

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')               

        var data = $('body').data ('data')
  
        query ({type: 'houses', part: 'contracts'}, {}, function (d) {
        
            var lines = []
        
            for (var k in d) {
            
                $.each (d [k], function () {                    
                    this.type = k
                    this.customer_label = this.org_customer_label || this.ind_customer_label
                    this.recid = this.type + '/' + this.id
                    lines.push (this)                
                })
            
            }
            
            data.lines = lines
                
            done (data)            
        
        }) 
  
    
    }

})