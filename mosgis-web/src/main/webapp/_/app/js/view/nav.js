define ([], function () {
    
    return function (data, view) {

        var $nav = $('nav')
           
        if ($_USER) {
        
            fill (view, data, $nav.show ())
            
            $('body > nav header button').after ('<hr>')
            $('body > nav footer button').before ('<hr>')
            
            $('body > nav header button').each (function () {
            
                var $this = $(this)
                
                if (location.href.indexOf ($this.attr ('name').substr (5)) < 0) return
                
                $this.addClass ('active')
                
                return false
            
            })
            
/*            
            var color = data.stand.color
            
            if (color) {            
                $('nav').css ({background: color})
                $('nav button').css ({'border-color': color})            
            }
*/        
        }

    }

});