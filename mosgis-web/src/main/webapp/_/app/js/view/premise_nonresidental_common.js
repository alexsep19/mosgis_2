define ([], function () {
    
    var form_name = 'premise_nonresidental_common_form'

    return function (data, view) {
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            data.item.__allow_annul = data.__allow_annul
            data.item.__allow_edit = data.__allow_edit
            
            var r = clone (data.item)

            if (/-/.test (r.terminationdate)) r.terminationdate = dt_dmy (r.terminationdate.substr (0, 10))

            w2ui [form_name].record = r
            
            $('div[data-block-name=premise_nonresidental_common] input').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))
        
        data.item.annulmentreason_label = data.vc_nsi_330 [data.item.annulmentreason]

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 230},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [],
                        onClick: $_DO.choose_tab_premise_nonresidental_common
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
            
                    {name: 'premisesnum', type: 'text'},
                    {name: 'cadastralnumber', type: 'text'},

                    {name: 'iscommonproperty', type: 'list', options: {
                        items: [{id: "0", text: "Нет"},{id: "1", text: "Да"}]
                    }},

                    {name: 'totalarea', type: 'float', options: {min: 0}},

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