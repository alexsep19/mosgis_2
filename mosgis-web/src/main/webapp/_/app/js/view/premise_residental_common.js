define ([], function () {
    
    var form_name = 'premise_residental_common_form'

    return function (data, view) {
    
        var entrances = data.tb_entrances.items
        entrances.unshift ({id: "", text: "Отдельный вход"})  

        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            data.item.__allow_annul = data.__allow_annul
            data.item.__allow_edit = data.__allow_edit
            
            var r = clone (data.item)

            if (/-/.test (r.terminationdate)) r.terminationdate = dt_dmy (r.terminationdate.substr (0, 10))

            w2ui [form_name].record = r
            
            $('div[data-block-name=premise_residental_common] input').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'premise_residental_layout',
            
            panels: [
                
                {type: 'top', size: 290},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'premise_residental_common_living_rooms', caption: 'Комнаты', off: data.item.code_vc_nsi_30 != '2'},
                        ].filter (not_off),
                        onClick: $_DO.choose_tab_premise_residental_common
                    }                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        
        var $panel = $(w2ui ['premise_residental_layout'].el ('top'))
        
        data.item.annulmentreason_label = data.vc_nsi_330 [data.item.annulmentreason]

        fill (view, data.item, $panel)        

        $panel.w2reform ({ 
        
            name   : form_name,
            
            record : data.item,                
            
            fields : [          
            
                    {name: 'uuid_entrance', type: 'list', options: {
                        items: entrances
                    }},
                    {name: 'premisesnum', type: 'text'},
                    {name: 'cadastralnumber', type: 'text'},
                    {name: 'code_vc_nsi_30', type: 'list', options: {
                        items: data.vc_nsi_30.items
                    }},
                    {name: 'totalarea', type: 'float', options: {min: 0}},
                    {name: 'grossarea', type: 'float', options: {min: 0}},
                    {name: 'terminationdate', type: 'date', options: {
//                        min: new Date(min_year, 1, 1, 0, 0, 0, 0), 
                        max: (new Date ())
                    }},
                                
            ].filter (not_off),

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})