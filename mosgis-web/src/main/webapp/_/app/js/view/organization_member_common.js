define ([], function () {

    var form_name = 'organization_member_common_form'

    $_DO.onchange_participant = function (value_new) {

        var participant_off = {
            dt_from : 35, // не член товарищества
            dt_to: 35,
            dt_from_participant: 10, // не включен в состав
            dt_to_participant: 10,
            dt_from_chairman: 10,
            dt_to_chairman: 10
        }

        var form = w2ui [form_name]
        var participant = value_new.id

        $.each(Object.keys(participant_off), function(){

            var field = this
            var off = participant_off [field] == participant

            form.get(field).$el.prop('disabled', off)

            if (off) {
                delete form.record [field]
            }
        })
    }

    $_DO.onchange_is_chairman = function (value_new) {

        var off_when_is_chairman = {
            dt_from_chairman: false,
            dt_to_chairman: false
        }

        var form = w2ui [form_name]

        $.each(Object.keys(off_when_is_chairman), function () {

            var field = this
            var off = off_when_is_chairman [field] == value_new

            form.get(field).$el.prop('disabled', off)

            if (off) {
                delete form.record [field]
            }
        })
    }

    function is_parent_gsk(data) {
        return data.vc_orgs_nsi_20
            .filter(function (i) { return i.code == 22 }) // ЖСК
            .length > 0
    }

    return function (data, view) {

        $_F5 = function (data) {

            data.item.__read_only = data.__read_only

            var r = clone (data.item)

            var f = w2ui [form_name]

            f.record = r

            $('div[data-block-name=organization_member_common] input').prop ({disabled: data.__read_only})
            $('input.edit-gsk:not(:disabled)').prop('disabled', !is_parent_gsk (data))

            if (!data.__read_only) {
                $_DO.onchange_participant(r.participant)
                $_DO.onchange_is_chairman(r.is_chairman == 1)
            }

            if ($_DO.update_participant_status) {
                $_DO.update_participant_status(data.item)
            }

            $('#file_from').toggle(!data.__read_only)
            $('#file_from_ro').toggle(data.__read_only)

            if (it.file_from) {

                clickOn($('#file_from_link'), $_DO.download_file_from_organization_member_common)
            }

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({

            name: 'organization_member_common_layout',

            panels: [

                {type: 'top', size: 308},
                {type: 'main', size: 350,
                    tabs: {
                        tabs:    [
                            {id: 'organization_member_common_log', caption: 'История изменений'},
                            {id: 'organization_member_property_documents', caption: 'Информация о собственности'},
                        ].filter (not_off),
                        onClick: $_DO.choose_tab_organization_member_common
                    }
                },

            ],

            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },

        });

        var it = data.item

        if (it.uuid_org == $_USER.uuid_org) it.is_own = 1

        var $panel = $(w2ui ['organization_member_common_layout'].el ('top'))

        fill (view, it, $panel)

        $panel.w2reform ({

            name   : form_name,

            record : data.item,

            fields : [
                {name: 'participant', type: 'list', options: {items: data.vc_org_prtcps.items}},
                {name: 'dt_from_participant', type: 'date'},
                {name: 'dt_to_participant', type: 'date'},

                {name: 'is_chairman', type: 'checkbox'},
                {name: 'dt_from_chairman', type: 'date'},
                {name: 'dt_to_chairman', type: 'date'},

                {name: 'phone', type: 'text'},
                {name: 'fax', type: 'text'},
                {name: 'mail', type: 'text'},

                {name: 'entrance_fee', type: 'float', options: {min: 0, max: 1000000000}},
                {name: 'contribution_share', type: 'float', options: {min: 0, max: 1000000000}},

                {name: 'dt_from', type: 'date'},
                {name: 'files', type: 'file', options: {
                        max: 1,
                        maxWidth: 290,
                        items: data.files
                 }},
                {name: 'file_from', type: 'text'},
                {name: 'dt_to', type: 'date'}
            ],

            focus: -1,

            onChange: function (e) {

                var form = this

                var handler = "onchange_" + e.target

                if ($_DO [handler]) {

                    e.done(function () {

                        $_DO [handler](e.value_new)

                        form.refresh()
                    })
                }
            },
        })

        $_F5 (data)

    }

})