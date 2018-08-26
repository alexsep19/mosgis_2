define ([], function () {
    
    var form_name = 'house_passport_form'
    
    function recalc_hidden_td () {
    
        var $hb = $('#hasblocks')
        var $hm = $('#hasmultiplehouseswithsameadres')
    
        if ($hb.prop ('checked')) {
            $hm.prop ('disabled', $('#totalsquare').prop ('disabled'))
        }
        else {
            $hm.prop ('checked', 0).prop ('disabled', 1)
        }
            
    }

    return function (data, view) {

        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only

            w2ui [form_name].record = data.item

            $('div[data-block-name=house_passport] input').prop ({disabled: data.__read_only})
            
            if (!data.__read_only && $('body').data ('data').has_blocks) {

                $('#hasblocks').prop ({
                    disabled: 1,
                    title: "К дому привязаны блоки.\nДля изменения характеристики необходимо удалить блоки\nили отправить в ГИС ЖКХ причину аннулирования блоков."
                })

            }                    

            w2ui [form_name].refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: data.item.is_condo ? 335 : 365},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    data.tabs.map (function (i) {return {id: i.id, caption: i.label}}),
                        onClick: $_DO.choose_tab_house_passport
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
            
                {name: 'code_vc_nsi_24', type: 'list', options: {items: data.vc_nsi_24.items}},

                {name: 'totalsquare', type: 'float', options: {min: 0, precision: 4, autoFormat: true}},
                {name: 'usedyear', type: 'int', options: {min: 1600, max: 2215, autoFormat: false}},
                {name: 'floorcount', type: 'int', options: {min: 1, max: 100}},
                {name: 'undergroundfloorcount', type: 'int', options: {min: 1, max: 100}},
                {name: 'minfloorcount', type: 'int', options: {min: 1, max: 100}},

                {name: 'culturalheritage', type: 'list', options: {items: [{id: "0", text: "Нет"}, {id: "1", text: "Да"}]}},
                {name: 'kad_n', type: 'text', options: {}},
                {name: 'hasblocks', type: 'checkbox', options: {}, off: data.item.is_condo},
                {name: 'hasmultiplehouseswithsameadres', type: 'checkbox', options: {}, off: data.item.is_condo},
                                
            ].filter (not_off),
            
            onRefresh: function (e) {e.done (
                
                function () {

                    clickOn ($('span.anchor'), onDataUriDblClick)
                    
                    recalc_hidden_td ()
                    
                    $('#hasblocks').off ('click').on ('click', recalc_hidden_td)
                    
                    if (sessionStorage.getItem ('check_sum_area_fields_of_a_house')) {
                        sessionStorage.removeItem ('check_sum_area_fields_of_a_house')
                        setTimeout ($_DO.check_sum_area_fields_of_a_house, 400)
                    }

                }

            )},
            
            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})