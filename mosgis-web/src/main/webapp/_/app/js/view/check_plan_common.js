define ([], function () {
    
    var form_name = 'check_plan_common_form'
    
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
            
            $('div[data-block-name=check_plan_common] input').prop ({disabled: data.__read_only})

            recalc ()

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 153},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'check_plan_common_log', caption: 'История изменений'},
                            {id: 'check_plan_examinations', caption: 'Проверки'},
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
            ],

            onChange: function (e) {
                if (e.target == 'shouldberegistered')
                    e.done (recalc)
            },

            onRender: function (e) { e.done (setTimeout (recalc, 100)) },

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})