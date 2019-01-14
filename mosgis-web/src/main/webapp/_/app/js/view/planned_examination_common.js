define ([], function () {
    
    var form_name = 'planned_examination_common_form'
    
    return function (data, view) {
        
        function recalc () {

            $('#uriregistrationplannumber').prop ('disabled', true)

            var r = w2ui [form_name].record

            if (r.shouldberegistered.id && !r.__read_only) $('#uriregistrationplannumber').prop ('disabled', false)

        }

        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)
            
            var f = w2ui [form_name]
            
            f.record = r
            
            $('div[data-block-name=planned_examination_common] input').prop ({disabled: data.__read_only})

            //recalc ()

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))     
                
        $panel.w2reform ({
        
            name   : form_name,
            
            record : data.item,                
            
            fields : [
                {name: 'uriregistrationplannumber', type: 'text'},
            ],

            onChange: function (e) {
                if (e.target == 'shouldberegistered')
                    e.done (recalc)
            },

            //onRender: function (e) { e.done (setTimeout (recalc, 100)) },

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})