define ([], function () {

    return function (data, view) {

        function has_territories () {

            perms = data.vc_orgs_nsi_20.filter (perm => perm.code == 8)
            return perms.length == 1

        }

        function has_coop_members() {

            var tsg_gsk_gk_other = ["19", "22", "20", "21"]

            var perms = data.vc_orgs_nsi_20.filter(function (i) {
                return tsg_gsk_gk_other.indexOf(i.code) >= 0
            })

            return perms.length == 1
            
        }
        
        function has_bank_accounts () {

            return data.vc_orgs_nsi_20.filter (function (i) {            
                return ["1", "2", "14", "18", "19", "22", "20", "21", "36"].indexOf (i.code) >= 0
            }).length > 0

        }

        var it = data.item

        it.vc_organization_types_label = it ['vc_organization_types.label']

        $('title').text (it.label)

        fill (view, it, $('#body'))

        $('#main_container').w2relayout({

            name: 'voc_organization_legal_layout',

            panels: [

                {type: 'main', size: 300,

                    tabs: {

                        tabs: [
                            {id: 'voc_organization_legal_main', caption: 'Юридическое лицо'},
                            {id: 'voc_organization_legal_info', caption: 'Информация'},
                            {id: 'voc_organization_legal_bank_accounts', caption: 'Платёжные реквизиты', off: !has_bank_accounts ()},
                            {id: 'voc_organization_legal_hours', caption: 'Режим работы'},
                            {id: 'voc_organization_legal_territories', caption: 'Территории', off: !has_territories ()},
                            {id: 'voc_organization_legal_members', caption: 'Члены ТСЖ/кооператива', off: !has_coop_members()},
                        ].filter(not_off),

                        onClick: $_DO.choose_top_tab_voc_organization_legal

                    }

                },
            ],

            onRender: function (e) {
                clickActiveTab(this.get('main').tabs, 'voc_organization_legal.active_top_tab')
            },

        });
    }

})