define ([], function () {
    
    var form_name = 'check_plan_common_form'
    
    return function (data, view) {
        
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)
            
            var f = w2ui [form_name]
            
            f.record = r
            
            $('div[data-block-name=check_plan_common] input').prop ({disabled: data.__read_only})

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 200},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'check_plan_common_log', caption: 'История изменений'},
                        ].filter (not_off),
                        onClick: $_DO.choose_tab_check_plan_common
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
                {name: 'shouldberegistered', type: 'list', options: {items: [
                    {id: 0, text: 'Нет'},
                    {id: 1, text: 'Да'},
                ]}},
                {name: 'uriregistrationplannumber', type: 'text'},
                {name: 'sign', type: 'list', options: {items: [
                    {id: 0, text: 'Проект'},
                    {id: 1, text: 'Подписан'},
                ]}},
            ],

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})