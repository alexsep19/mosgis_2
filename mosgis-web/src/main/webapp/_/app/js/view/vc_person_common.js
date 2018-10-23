define ([], function () {
    
    var form_name = 'vc_person_common_form'

    return function (data, view) {
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)

            w2ui [form_name].record = r
            
            $('div[data-block-name=vc_person_common] input').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 400},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'vc_person_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_vc_person_common
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
                {name: 'surname', type: 'text'},
                {name: 'firstname', type: 'text'},
                {name: 'patronymic', type: 'text'},
                {name: 'is_female', type: 'list', options: {items: [{id:"", text: "Не указано"}, {id:0, text: "Мужской"}, {id: 1, text: "Женский"}]}},
                {name: 'placebirth', type: 'text'},
                {name: 'birthdate', type: 'date'},
                {name: 'snils', type: 'text'},
                {name: 'code_vc_nsi_95', type: 'list', options: {items: data.vc_nsi_95.items}},
                {name: 'series', type: 'text'},
                {name: 'number_', type: 'text'},
                {name: 'issuedate', type: 'date'},
                {name: 'issuer', type: 'text'},
            ],

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})