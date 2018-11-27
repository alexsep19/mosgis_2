define ([], function () {
    
    var form_name = 'vote_decision_list_common_form'

    var read_only = false

    return function (data, view) {

        read_only = true;

        function recalc () {

            var tables = {'11.1': 'management_type_table',
                          '2.1': 'forming_fund_table'
            }

            function disable_block (table_name) {
                var $table = $('#' + table_name)
                $table.find('input').each (disable_element)
                $table.find('label').each (disable_element)
            }

            function enable_block (table_name) {
                var $table = $('#' + table_name)
                $table.find('input').each (enable_element)
                $table.find('label').each (enable_element)
            }

            function disable_element (i, element) {
                $(element).prop ('disabled', true)
            }

            function enable_element (i, element) {
                $(element).prop ('disabled', false)
            }

            for (var table in tables) {
                disable_block (tables[table])
            }

            var v = w2ui [form_name].values ()

            if (!read_only) enable_block(tables[v.decisiontype_vc_nsi_63])

            $panel_top = $('#layout_passport_layout_panel_top')
            $panel_main = $('#layout_passport_layout_panel_main')
            $top_form_box = $panel_top.children ('.w2ui-panel-content').children ('.w2ui-form-box')

            $panel_top.height (350)
            $top_form_box.height (350)
            $panel_main.css('top', '351px')

        }

        $_F5 = function (data) {
        
            read_only = data.__read_only

            var r = clone (data.item)

            w2ui [form_name].record = r

            w2ui [form_name].record['__read_only'] = read_only
            
            $('div[data-block-name=vote_decision_list_common] input').prop ({disabled: data.__read_only})
            $('div[data-block-name=vote_decision_list_common] textarea').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()

            recalc ()

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
                            {id: 'vote_decision_list_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_vote_decision_list_common
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
                            {name: 'decisiontype_vc_nsi_63', type: 'list', options: {items: data.vc_nsi_63.items}},
                            {name: 'questionname', type: 'textarea'},
                            {name: 'managementtype_vc_nsi_25', type: 'list', options: {items: data.vc_nsi_25.items}},
                            {name: 'formingfund_vc_nsi_241', type: 'list', options: {items: data.vc_nsi_241.items}},
                            {name: 'agree', type: 'text'},
                            {name: 'against', type: 'text'},
                            {name: 'abstent', type: 'text'},
                            {name: 'votingresume', type: 'list', options: {items: [
                                {id: "M", text: "Решение принято"},
                                {id: "N", text: "Решение не принято"},
                            ]}},
                        ],

            onChange: function (e) {if (e.target == "decisiontype_vc_nsi_63") e.done (recalc)},
                        
            onRender: function (e) {e.done (setTimeout (recalc, 100))}
            
        })

        $_F5 (data)     

    }
    
})