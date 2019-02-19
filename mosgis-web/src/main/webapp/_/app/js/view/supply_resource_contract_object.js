define ([], function () {

    return function (data, view) {

        var it = data.item

        $('title').text ('Объект жилищного фонда по адресу ' +  +it['building.label'] + ' ДРС №' + it['sr_ctr.label'])

        fill (view, it, $('#body'))

        $('#container').w2relayout ({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'supply_resource_contract_object_common',   caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_supply_resource_contract_object

                    }

                },

            ],

            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'supply_resource_contract_object.active_tab')
            },

        });

        if (it['sr_ctr.uuid']) {
            clickOn ($('#lnk_sr_ctr'), function () {openTab ('/supply_resource_contract/' + it['sr_ctr.uuid'])})
        }

        if (it['house.uuid']) {
            clickOn($('#lnk_house'), function () {
                openTab('/house/' + it['house.uuid'])
            })
        }
    }

})