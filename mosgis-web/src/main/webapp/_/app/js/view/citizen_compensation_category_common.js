define ([], function () {
    
    var form_name = 'citizen_compensation_category_common_form'

    return function (data, view) {
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)

            w2ui [form_name].record = r
            
            $('div[data-block-name=citizen_compensation_category_common] input').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 270},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'citizen_compensation_category_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_citizen_compensation_category_common
                    }                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        
        var $panel = $(w2ui ['passport_layout'].el ('top'))
                
        fill (view, data.item, $panel)        

        data.voc_bool = [
            {id: "0", text: "Нет"},
            {id: "1", text: "Да"}
        ]

        $panel.w2reform ({ 
        
            name   : form_name,
            
            record : data.item,                
            
            fields : [
            ],

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})