define ([], function () {
    
    var form_name = 'living_room_common_form'

    return function (data, view) {
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)

            if (/-/.test (r.terminationdate)) r.terminationdate = dt_dmy (r.terminationdate.substr (0, 10))

            w2ui [form_name].record = r
            
            $('div[data-block-name=living_room_common] input').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 250},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [],
                        onClick: $_DO.choose_tab_living_room_common
                    }                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        
        var $panel = $(w2ui ['passport_layout'].el ('top'))
        
        data.item.annulmentreason_label = data.vc_nsi_330 [data.item.annulmentreason]
        
        fill (view, data.item, $panel)        

        $panel.w2reform ({ 
        
            name   : form_name,
            
            record : data.item,                
            
            fields : [                     
                    {name: 'roomnumber', type: 'text'},
                    {name: 'cadastralnumber', type: 'text'},
                    {name: 'square', type: 'float', options: {min: 0}},                                                    
            ],

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})