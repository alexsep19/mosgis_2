define ([], function () {
    
    var form_name = 'vote_decision_list_common_form'

    return function (data, view) {

        var read_only = true;
        var changed = false;
        var canceled = false;

        function change_val (name, value) {
            $('#' + name).val (value)
            $('#' + name).trigger ('change')
        }

        function recalc () {

            var tables = {'11.1': 'management_type_table',
                          '2.1': 'forming_fund_table'
            }

            var sizes = {'management_type_table': 314,
                         'forming_fund_table': 314,
                         'default': 276}

            function disable_block (table_name) {
                var $table = $('#' + table_name)
                $table.find('input').each (disable_element)
                $table.find('label').each (disable_element)
                $table.hide ()
            }

            function enable_block (table_name) {
                var $table = $('#' + table_name)
                $table.find('input').each (enable_element)
                $table.find('label').each (enable_element)
                $table.show ()
            }

            function disable_element (i, element) {
                $(element).prop ('disabled', true)
            }

            function enable_element (i, element) {
                if (read_only != undefined && !read_only) {
                    $(element).prop ('disabled', false)
                }
            }

            for (var table in tables) {
                disable_block (tables[table])
            }

            var v = w2ui [form_name].values ()
            var table_name = 'default'

            if (tables[v.decisiontype_vc_nsi_63])
                table_name = tables[v.decisiontype_vc_nsi_63]

            enable_block(table_name)
            if (changed) {
                if (canceled || v.decisiontype_vc_nsi_63 == data.item.decisiontype_vc_nsi_63) change_val ('questionname', data.item.questionname)
                else change_val ('questionname', data.vc_nsi_63[v.decisiontype_vc_nsi_63])
                canceled = false
            }

            $panel_top = $('#layout_passport_layout_panel_top')
            $panel_main = $('#layout_passport_layout_panel_main')
            $top_form_box = $panel_top.children ('.w2ui-panel-content').children ('.w2ui-form-box')

            $panel_top.height (sizes[table_name])
            $top_form_box.height (sizes[table_name])
            $panel_main.css('top', sizes[table_name] + 1 + 'px')

        }

        $_F5 = function (data, cancel) {

            read_only = data.__read_only
            canceled = cancel

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

            onChange: function (e) {if (e.target == "decisiontype_vc_nsi_63") changed = true; e.done (recalc)},
                        
            onRender: function (e) {e.done (setTimeout (recalc, 100))}
            
        })

        $_F5 (data)     

    }
    
})