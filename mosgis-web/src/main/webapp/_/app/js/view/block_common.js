define ([], function () {
    
    var form_name = 'block_common_form'

    return function (data, view) {
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            data.item.__allow_annul = data.__allow_annul
            data.item.__allow_edit = data.__allow_edit
            
            var r = clone (data.item)

            if (/-/.test (r.terminationdate)) r.terminationdate = dt_dmy (r.terminationdate.substr (0, 10))

            w2ui [form_name].record = r
            
            $('div[data-block-name=block_common] input').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: data.item.is_nrs ? 210 : 310},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'block_common_living_rooms', caption: 'Комнаты', off: data.item.is_nrs},
                        ],
                        onClick: $_DO.choose_tab_block_common
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
            
                    {name: 'blocknum', type: 'text'},
                    {name: 'cadastralnumber', type: 'text'},
                    {name: 'code_vc_nsi_30', type: 'list', options: {
                        items: data.vc_nsi_30.items
                    }, _no_nrs: 1},
                    {name: 'totalarea', type: 'float', options: {min: 0}},
                    {name: 'grossarea', type: 'float', options: {min: 0}, _no_nrs: 1},
                    {name: 'f_20002', type: 'list', options: {
                        items: data.vc_nsi_14.items
                    }, _no_nrs: 1},
                                                    
            ],

                onChange: function (e) {

                    var form = this

                    if (e.target == "is_nrs") {

                        e.done (function () {

                            var off = e.value_new.id == 1
                            
                            $.each (form.fields, function () {                            
                                if (!this._no_nrs) return
                                this.$el.prop ('disabled', off)
                                if (off) delete form.record [this.name]
                            })
                            
                            if (!off && !form.record.code_vc_nsi_30) form.record.code_vc_nsi_30 = data.vc_nsi_30.items [2]

                            form.refresh ()
                            
                        })

                    }               

                },
/*                
                onRefresh: function (e) {
                
                    var form = w2ui [e.target]
                    
                    e.done (function () {
                        if (form.record.is_nrs) $('div[data-off=is_nrs]').hide ()
                    }) 
                                
                },
*/
            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})