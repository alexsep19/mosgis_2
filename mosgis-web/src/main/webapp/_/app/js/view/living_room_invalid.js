define ([], function () {

    var form_name = 'living_room_invalid_form'

    return function (data, view) {

        $_F5 = function (data) {

            var r = clone (data.item)

            if (/-/.test (r.f_20133)) r.f_20133 = dt_dmy (r.f_20133.substr (0, 10))

            r.__read_only = data.__read_only            

            w2ui [form_name].record = r

            $('div[data-block-name=living_room_invalid] input').prop ({disabled: r.__read_only})
                        
            w2ui [form_name].refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 200},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [],
                        onClick: $_DO.choose_tab_living_room_invalid
                    }                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        
        var $panel = $(w2ui ['passport_layout'].el ('top'))
        
        fill (view, data.item, $panel)        

        $panel.w2reform ({ 
        
            name   : form_name,
            
            record : data.item,                
            
            fields : [          
            
                    {name: 'f_20132', type: 'list', options: {
                        items: data.vc_nsi_273.items
                    }},
                    {name: 'f_20133', type: 'date', options: {
//                        min: new Date(min_year, 1, 1, 0, 0, 0, 0), 
                        max: (new Date ())
                    }},
                    {name: 'f_20134', type: 'text'},
                    {name: 'file', type: 'file', options: {max: 1}},
                                
            ].filter (not_off),

            focus: -1,
            
            onRefresh: function (e) {
            
                e.done (function () {
                
                    var file = $('body').data ('data').file
                
                    if (this.record.__read_only) {
                    
                        $('#file_input_div').hide ()
                        
                        if (file) {
                            $('#file_label').text (file.label)
                            clickOn ($('#file_label'), $_DO.download_living_room_invalid)
                        }
                        else {
                            $('#file_link_div').hide ()
                        }
                        
                    }
                    else {
                        $('#file_input_div').show ()
                    }                    
                                
                })
            
            }
            
        })

        $_F5 (data)        

    }
    
})